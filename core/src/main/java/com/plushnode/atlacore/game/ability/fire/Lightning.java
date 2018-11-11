package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.math.LineSegment;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class Lightning implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private World world;
    private Random rand;
    private State state;
    private List<Collider> colliders = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.world = user.getWorld();
        this.rand = new Random();
        this.state = new ChargeState();

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        for (Collider collider : colliders) {
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    return false;
                }

                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).damage(userConfig.damage, user);
                }
                return false;
            }, true);
        }

        if (!state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Lightning";
    }

    @Override
    public Collection<Collider> getColliders() {
        return colliders;
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    private void displayParticle(Location location) {
        Game.plugin.getParticleRenderer().displayColored(ParticleEffect.REDSTONE, 1, 225, 255, 0.004f, 0, location, 257);
    }

    private void displayParticle(Location location, double deviation) {
        Vector3D offset = new Vector3D(
                (rand.nextDouble() * deviation) - (deviation / 2.0),
                (rand.nextDouble() * deviation) - (deviation / 2.0),
                (rand.nextDouble() * deviation) - (deviation / 2.0)
        );

        Game.plugin.getParticleRenderer().displayColored(ParticleEffect.REDSTONE, 1, 225, 255, 0.004f, 0, location.add(offset), 257);
    }

    private interface State {
        boolean update();
    }

    private class ChargeState implements State {
        private long startTime;

        public ChargeState() {
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();

            boolean charged = time > startTime + userConfig.chargeTime;

            if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
                return false;
            }

            if (charged) {
                if (!user.isSneaking()) {
                    // Transition to travel state
                    state = new TravelState();
                    return true;
                } else {
                    Location location = user.getEyeLocation().add(user.getDirection());
                    displayParticle(location);
                }

                return true;
            }

            if (!user.isSneaking()) {
                return false;
            }

            // TODO: Come up with better charge animation
            double t = Math.cos(time * 0.0075);

            Vector3D side = VectorUtil.normalizeOrElse(Vector3D.PLUS_J.crossProduct(user.getDirection()), Vector3D.PLUS_I);

            Location location = user.getEyeLocation().add(side.scalarMultiply(t));
            displayParticle(location, 0.3);
            return true;
        }
    }

    // This class is for moving the main lightning bolt.
    private class TravelState implements State {
        private Bolt bolt;
        private Location origin;
        private Location location;
        private Location target;
        private double distance;
        private long startTime;

        public TravelState() {
            location = user.getEyeLocation();
            this.origin = location;

            target = RayCaster.cast(user, new Ray(location, user.getDirection()), userConfig.range, true, false);
            Location entityTarget = RayCaster.cast(user, new Ray(location, user.getDirection()), userConfig.range, true, true);

            // Don't add a mid point if there was no entity intersection.
            if (user.getLocation().distanceSquared(entityTarget) >= user.getLocation().distanceSquared(target)) {
                entityTarget = null;
            }

            this.distance = target.distance(location);
            this.startTime = System.currentTimeMillis();
            this.bolt = new Bolt(origin, target, entityTarget);

            user.setCooldown(Lightning.this, userConfig.cooldown);
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();
            double elapsedSeconds = (time - startTime) / 1000.0;
            double totalSeconds = distance / (userConfig.speed * 20);
            double t = elapsedSeconds / totalSeconds;
            double step = 4.0 / (distance / (50.0 / 1000.0));
            double prevStart = Math.max(0.0, t - 0.5);

            // Render recent previous locations.
            for (double prevT = prevStart; prevT <= t; prevT += step) {
                for (Location location : bolt.interpolate(prevT)) {
                    displayParticle(location);
                }
            }

            for (Location current : bolt.interpolate(t)) {
                if (!Game.getProtectionSystem().canBuild(user, current)) {
                    return false;
                }

                colliders.add(new Sphere(current.toVector(), userConfig.collisionRadius, world));
            }

            return t < 1.0;
        }
    }

    private class Bolt {
        List<LineSegment> segments = new ArrayList<>();
        private Location start;
        private Location end;
        private double distance;

        public Bolt(Location start, Location end, Location midTarget) {
            this.start = start;
            this.end = end;
            this.distance = end.distance(start);

            generateSegments(userConfig.boltGenerations, midTarget);
        }

        public List<Location> interpolate(double t) {
            List<Location> locations = new ArrayList<>();

            t = Math.max(0.0, Math.min(t, 1.0));

            for (LineSegment segment : segments) {
                double tStart = segment.start.distance(start.toVector()) / distance;
                double tEnd = tStart + (segment.length() / distance);

                if (t >= tStart && t <= tEnd) {
                    double segT = (t - tStart) / (tEnd - tStart);
                    Vector3D pos = segment.start.add(segment.end.subtract(segment.start).scalarMultiply(segT));

                    locations.add(user.getWorld().getLocation(pos));
                }
            }

            return locations;
        }

        // Generates segments along the main line and offsets them create some wiggle.
        private void generateSegments(int generations, Location midTarget) {
            double maxOffset = userConfig.boltMaxOffset;

            if (midTarget != null) {
                segments.add(new LineSegment(start, midTarget));
                segments.add(new LineSegment(midTarget, end));
            } else {
                segments.add(new LineSegment(start, end));
            }

            for (int i = 0; i < generations; ++i) {
                List<LineSegment> previousSegments = new ArrayList<>(segments);
                segments.clear();

                for (LineSegment segment : previousSegments) {
                    Vector3D mid = segment.interpolate(0.5);

                    Vector3D offset = getOffset(segment.getDirection(), maxOffset);

                    Location castResult = RayCaster.cast(user.getWorld(), new Ray(mid, offset.normalize()), offset.getNorm(), false);
                    mid = castResult.toVector();

                    segments.add(new LineSegment(segment.start, mid));
                    segments.add(new LineSegment(mid, segment.end));

                    if (Math.random() < userConfig.boltForkChance) {
                        Vector3D direction = mid.subtract(start.toVector()).normalize();
                        Vector3D forkEnd = mid.add(getOffset(direction, Math.min(maxOffset * userConfig.boltForkLength, userConfig.boltMaxOffset * 0.75)));
                        segments.add(new LineSegment(mid, forkEnd));
                    }
                }

                maxOffset /= 2.0;
            }
        }

        private Vector3D getOffset(Vector3D direction, double maxOffset) {
            double t = (rand.nextDouble() * maxOffset * 2) - maxOffset;

            Vector3D side = VectorUtil.normalizeOrElse(Vector3D.PLUS_J.crossProduct(direction), Vector3D.PLUS_I);

            // Get a random vector moving the midpoint to the side by some amount
            Vector3D first = side.scalarMultiply(t);

            t = (rand.nextDouble() * maxOffset * 2) - maxOffset;

            // Get a random vector moving the midpoint up/down by some amount
            Vector3D up = side.crossProduct(direction);
            Vector3D second = up.normalize().scalarMultiply(t);
            return first.add(second);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.CHARGE_TIME)
        public long chargeTime;
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double collisionRadius;
        public int boltGenerations;
        public double boltMaxOffset;
        public double boltForkChance;
        public double boltForkLength;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "lightning");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            range = abilityNode.getNode("range").getDouble(20.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            damage = abilityNode.getNode("damage").getDouble(4.0);
            chargeTime = abilityNode.getNode("charge-time").getLong(2500);
            collisionRadius = abilityNode.getNode("collision-radius").getDouble(1.5);

            CommentedConfigurationNode bolt = abilityNode.getNode("bolt");

            boltGenerations = bolt.getNode("generations").getInt(5);
            boltMaxOffset = bolt.getNode("max-offset").getDouble(3.0);
            boltForkChance = bolt.getNode("fork-chance").getDouble(0.3);
            boltForkLength = bolt.getNode("fork-length").getDouble(1.7);
        }
    }
}
