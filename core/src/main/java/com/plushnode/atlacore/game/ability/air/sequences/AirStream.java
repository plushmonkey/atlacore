package com.plushnode.atlacore.game.ability.air.sequences;

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
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class AirStream implements Ability {
    public static Config config = new Config();

    private User user;
    private Location location;
    private Location origin;
    private List<Flight> flights = new ArrayList<>();
    private CompositeRemovalPolicy removalPolicy;
    private Sphere collider;
    private List<TailData> tail = new ArrayList<>();
    private int tailIndex;
    private Map<Entity, Long> affected = new HashMap<>();
    private List<Entity> immune = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.location = user.getEyeLocation();
        this.origin = this.location;

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, config.range, () -> origin),
                new SneakingRemovalPolicy(user, true)
        );

        this.collider = new Sphere(location.toVector(), config.radius);

        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        Location previous = location;
        Vector3D direction = getDirection();
        location = location.add(direction.scalarMultiply(config.speed));
        collider = new Sphere(location.toVector(), config.radius);

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        if (CollisionUtil.handleBlockCollisions(collider, previous, location, true).getFirst()) {
            location = previous;
        }

        // TODO: There's probably a better way to handle this.
        // Either by eliminating vertical movement when max height is reached, or by dropping entities out.
        if (location.getY() - origin.getY() > config.height) {
            return UpdateResult.Remove;
        }

        handleTail(direction);

        CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
            if (!affected.containsKey(entity) && !immune.contains(entity)) {
                affected.put(entity, System.currentTimeMillis() + config.entityDuration);

                if (entity instanceof Player) {
                    flights.add(Flight.get((User)entity));
                }
            }

           return false;
        }, true);

        for (Iterator<Map.Entry<Entity, Long>> iterator = affected.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<Entity, Long> entry = iterator.next();
            Entity entity = entry.getKey();
            long end = entry.getValue();

            if (time > end) {
                if (entity instanceof Player) {
                    flights.removeIf((flight) -> flight.getUser().equals(entity));
                    new Flight.GroundRemovalTask((User)entity, 1, 20000);
                }
                immune.add(entity);
                iterator.remove();
                continue;
            }

            Vector3D force = location.subtract(entity.getLocation()).toVector();
            if (force.getNormSq() == 0)  {
                continue;
            }

            force = force.normalize().scalarMultiply(config.speed);
            entity.setVelocity(force);
        }

        return UpdateResult.Continue;
    }

    private void handleTail(Vector3D direction) {
        TailData newData = new TailData(location, direction);

        if (tail.size() <= tailIndex) {
            tail.add(newData);
        } else {
            tail.set(tailIndex, newData);
        }

        tailIndex = ++tailIndex % config.tailCount;

        for (TailData data : tail) {
            Vector3D side = Vector3D.PLUS_J.crossProduct(data.direction);

            if (side.getNormSq() > 0) {
                side = side.normalize();
            } else {
                side = Vector3D.PLUS_I;
            }

            for (double theta = 0; theta < Math.PI * 2; theta += Math.toRadians(45)) {
                Vector3D offset = VectorUtil.rotate(side, data.direction, theta).normalize().scalarMultiply(config.radius);

                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, data.location.add(offset), 257);
            }
        }
    }

    private Vector3D getDirection() {
        Location target = RayCaster.cast(user, new Ray(user.getEyeLocation(), user.getDirection()), config.range, true, true);
        Vector3D direction = target.subtract(location).toVector();

        if (direction.getNormSq() > 0) {
            direction = direction.normalize();
        }

        return direction;
    }

    @Override
    public void destroy() {
        user.setCooldown(this);

        for (Flight flight : flights) {
            User flightUser = flight.getUser();
            flight.release();

            new Flight.GroundRemovalTask(flightUser, 1, 20000);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirStream";
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(collider);
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    private static class TailData {
        public Location location;
        public Vector3D direction;

        TailData(Location location, Vector3D direction) {
            this.location = location;
            this.direction = direction;
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        double speed;
        double range;
        double radius;
        long entityDuration;
        double height;
        int tailCount;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "sequences", "airstream");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(7000);
            speed = abilityNode.getNode("speed").getDouble(0.5);
            range = abilityNode.getNode("range").getDouble(40.0);
            radius = abilityNode.getNode("radius").getDouble(0.5);
            height = abilityNode.getNode("height").getDouble(14.0);
            entityDuration = abilityNode.getNode("entity-duration").getLong(4000);
            tailCount = abilityNode.getNode("tail-count").getInt(10);
        }
    }
}
