package com.plushnode.atlacore.game.ability.air;

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
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class AirSuction implements Ability {
    private static final double BLOCK_COLLISION_RADIUS = 0.5;

    public static Config config = new Config();

    private User user;
    private Location origin;
    private Location start;
    private Location location;
    private Vector3D direction;
    private boolean launched;
    private boolean selectedOrigin;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        for (AirSuction suction : Game.getAbilityInstanceManager().getPlayerInstances(user, AirSuction.class)) {
            if (!suction.launched) {
                if (method == ActivationMethod.Sneak) {
                    suction.selectOrigin();

                    if (!Game.getProtectionSystem().canBuild(user, suction.origin)) {
                        Game.getAbilityInstanceManager().destroyInstance(user, suction);
                    }
                } else {
                    if (!suction.launch()) {
                        Game.getAbilityInstanceManager().destroyInstance(user, suction);
                        return false;
                    }
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
                new SwappedSlotsRemovalPolicy<>(user, AirSuction.class),
                new OutOfWorldRemovalPolicy(user)
        );

        if (method == ActivationMethod.Sneak) {
            selectOrigin();

            if (!Game.getProtectionSystem().canBuild(user, origin)) {
                return false;
            }
        } else {
            this.origin = user.getEyeLocation();

            if (!Game.getProtectionSystem().canBuild(user, origin)) {
                return false;
            }

            return launch();
        }

        return true;
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
                    0.275f, 0.275f, 0.275f, 0.0f, config.particles, location, 257);

            if (location.distanceSquared(origin) <= config.speed * config.speed) {
                return UpdateResult.Remove;
            }

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return UpdateResult.Remove;
            }

            Sphere collider = new Sphere(location.toVector(), config.entityCollisionRadius);

            // Handle user separately from the general entity collision.
            if (this.selectedOrigin) {
                if (user.getBounds().at(user.getLocation()).intersects(collider)) {
                    affect(user);
                }
            }

            CollisionUtil.handleEntityCollisions(user, collider, this::affect, false, false);

            Sphere blockCollider = new Sphere(location.toVector(), BLOCK_COLLISION_RADIUS);
            boolean collision = CollisionUtil.handleBlockCollisions(blockCollider, previous, location, true).getFirst();

            if (collision && doBlockCollision(previous)) {
                return UpdateResult.Remove;
            }
        }

        return UpdateResult.Continue;
    }

    // Don't do block collision on initial launch of selected area.
    private boolean doBlockCollision(Location previous) {
        return previous.distanceSquared(start) > BLOCK_COLLISION_RADIUS * BLOCK_COLLISION_RADIUS;
    }

    private boolean affect(Entity entity) {
        double factor = config.push;

        factor *= 1.0 - (location.distance(origin) / (2 * config.range));

        // Reduce the push if the player is on the ground.
        if (entity.equals(user) && WorldUtil.isOnGround(entity)) {
            factor *= 0.5;
        }

        Vector3D velocity = entity.getVelocity();
        // The strength of the entity's velocity in the direction of the blast.
        double strength = velocity.dotProduct(direction);

        if (strength > factor) {
            velocity = velocity.scalarMultiply(0.5).add(direction.scalarMultiply(strength / 2));
        } else if (strength > factor * 0.5) {
            velocity = velocity.add(direction.scalarMultiply(factor - strength));
        } else {
            velocity = velocity.add(direction.scalarMultiply(factor * 0.5));
        }

        entity.setVelocity(velocity);
        entity.setFireTicks(0);

        if (entity instanceof User) {
            // Give the target Flight so they don't take fall damage.
            new Flight.GroundRemovalTask((User)entity, 1, 20000);
        }

        return false;
    }

    private void selectOrigin() {
        Ray ray = new Ray(user.getEyeLocation(), user.getDirection());

        this.origin = RayCaster.cast(user.getWorld(), ray, config.selectRange, true);
        // Move origin back slightly so it doesn't collide with ground.
        this.origin = this.origin.subtract(user.getDirection().scalarMultiply(BLOCK_COLLISION_RADIUS));

        this.selectedOrigin = true;
    }

    private boolean launch() {
        this.launched = true;

        // Shrink the ray distance so it doesn't go over the cap.
        double rangeShrink = Math.max(0.0, user.getDirection().dotProduct(Vector3D.PLUS_J) * 1.8);
        Location target = RayCaster.cast(user, new Ray(user.getEyeLocation(), user.getDirection()), config.range - rangeShrink, true, true);
        this.location = target;
        this.start = location;
        this.direction = origin.subtract(target).toVector().normalize();

        removalPolicy.removePolicyType(IsDeadRemovalPolicy.class);
        removalPolicy.removePolicyType(OutOfRangeRemovalPolicy.class);
        removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);

        user.setCooldown(this);

        return location.distanceSquared(origin) <= config.range * config.range;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Collection<Collider> getColliders() {
        if (location == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new Sphere(location.toVector(), config.abilityCollisionRadius));
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirSuction";
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        double range;
        double speed;
        double entityCollisionRadius;
        double abilityCollisionRadius;
        int particles;
        double push;

        double selectRange;
        double selectOutOfRange;
        int selectParticles;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airsuction");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            range = abilityNode.getNode("range").getDouble(20.0);
            speed = abilityNode.getNode("speed").getDouble(1.25);
            particles = abilityNode.getNode("particles").getInt(6);
            push = abilityNode.getNode("push").getDouble(2.5);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(2.0);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(2.0);

            selectRange = abilityNode.getNode("select").getNode("range").getDouble(10.0);
            selectOutOfRange = abilityNode.getNode("select").getNode("out-of-range").getDouble(20.0);
            selectParticles = abilityNode.getNode("select").getNode("particles").getInt(4);
        }
    }
}
