package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.collision.Ray;
import com.plushnode.atlacore.collision.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsDeadRemovalPolicy;
import com.plushnode.atlacore.policies.removal.OutOfRangeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.SwappedSlotsRemovalPolicy;
import com.plushnode.atlacore.util.Flight;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AirBlast implements Ability {
    private static final double BLOCK_COLLISION_RADIUS = 0.5;

    public static Config config = new Config();
    private User user;
    private Location origin;
    private Location location;
    private Vector3D direction;
    private boolean launched;
    private boolean selectedOrigin;

    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        for (AirBlast blast : Game.getAbilityInstanceManager().getPlayerInstances(user, AirBlast.class)) {
            if (!blast.launched) {
                if (method == ActivationMethod.Sneak) {
                    blast.selectOrigin();

                    if (!Game.getProtectionSystem().canBuild(user, blast.origin)) {
                        Game.getAbilityInstanceManager().destroyInstance(user, blast);
                    }
                } else {
                    blast.launch();
                }

                return false;
            }
        }

        this.user = user;
        this.launched = false;
        this.selectedOrigin = false;

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, config.selectOutOfRange, () -> origin),
                new SwappedSlotsRemovalPolicy<>(user, AirBlast.class)
        );

        if (method == ActivationMethod.Sneak) {
            selectOrigin();
        } else {
            this.origin = user.getEyeLocation();
            launch();
        }

        return true;
    }

    private void selectOrigin() {
        Ray ray = new Ray(user.getEyeLocation(), user.getDirection());

        this.origin = Game.getCollisionSystem().castRay(user.getWorld(), ray, config.selectRange, true);
        // Move origin back slightly so it doesn't collide with ground.
        this.origin = this.origin.subtract(user.getDirection().scalarMultiply(BLOCK_COLLISION_RADIUS));

        this.selectedOrigin = true;
    }

    private void launch() {
        this.launched = true;
        this.location = origin;
        this.direction = user.getDirection();

        removalPolicy.removePolicyType(IsDeadRemovalPolicy.class);
        removalPolicy.removePolicyType(OutOfRangeRemovalPolicy.class);
        removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);

        user.setCooldown(this);
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (!launched) {
            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL,
                    (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(),
                    config.selectParticles, origin, 257);
        } else {
            Location previous = location;

            location = location.add(direction.scalarMultiply(config.speed));

            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL,
                    0.275f, 0.275f, 0.275f, 0.0f,
                    config.particles, location, 257);

            if (location.distanceSquared(origin) >= config.range * config.range) {
                return UpdateResult.Remove;
            }

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return UpdateResult.Remove;
            }

            Sphere collider = new Sphere(location.toVector(), config.entityCollisionRadius);

            // Handle user separately from the general entity collision.
            if (this.selectedOrigin) {
                if (Game.getCollisionSystem().getAABB(user).at(user.getLocation()).intersects(collider)) {
                    affect(user);
                    return UpdateResult.Remove;
                }
            }

            boolean hit = Game.getCollisionSystem().handleEntityCollisions(user, collider, this::affect, false, false);

            if (hit) {
                return UpdateResult.Remove;
            }


            boolean collision = Game.getCollisionSystem().collidesWithBlocks(user.getWorld(),
                    new Sphere(location.toVector(), BLOCK_COLLISION_RADIUS), previous, location, true);

            if (collision && doBlockCollision(previous)) {
                return UpdateResult.Remove;
            }
        }

        return UpdateResult.Continue;
    }

    // Don't do block collision on initial launch of selected area.
    private boolean doBlockCollision(Location previous) {
        if (!this.selectedOrigin) return true;

        return previous.distanceSquared(origin) > BLOCK_COLLISION_RADIUS * BLOCK_COLLISION_RADIUS;
    }

    private boolean affect(Entity entity) {
        double factor = config.otherPush;

        if (entity.equals(user)) {
            factor = config.selfPush;
        }

        factor *= 1.0 - (location.distance(origin) / (2 * config.range));

        entity.setVelocity(direction.scalarMultiply(factor));

        if (entity instanceof User) {
            // Give the target Flight so they don't take fall damage.
            new Flight.GroundRemovalTask((User)entity, 1, 20000);
        }

        return entity instanceof LivingEntity;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getName() {
        return "AirBlast";
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        double range;
        double speed;
        double entityCollisionRadius;
        int particles;
        double selfPush;
        double otherPush;

        double selectRange;
        double selectOutOfRange;
        int selectParticles;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airblast");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            range = abilityNode.getNode("range").getDouble(25.0);
            speed = abilityNode.getNode("speed").getDouble(1.25);
            particles = abilityNode.getNode("particles").getInt(6);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(2.0);

            selfPush = abilityNode.getNode("push").getNode("self").getDouble(2.5);
            otherPush = abilityNode.getNode("push").getNode("other").getDouble(3.5);

            selectRange = abilityNode.getNode("select").getNode("range").getDouble(10.0);
            selectOutOfRange = abilityNode.getNode("select").getNode("out-of-range").getDouble(35.0);
            selectParticles = abilityNode.getNode("select").getNode("particles").getInt(4);

            abilityNode.getNode("speed").setComment("How many meters the blast advances with each tick.");
        }
    }
}
