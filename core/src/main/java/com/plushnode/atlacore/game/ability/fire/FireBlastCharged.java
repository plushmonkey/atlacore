package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.policies.removal.OutOfWorldRemovalPolicy;
import com.plushnode.atlacore.policies.removal.SwappedSlotsRemovalPolicy;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.Collections;

public class FireBlastCharged implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private World world;
    private long startTime;
    private boolean launched;
    private Location origin;
    private Location location;
    private Vector3D direction;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (method != ActivationMethod.Sneak) {
            return false;
        }

        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.world = user.getWorld();
        this.launched = false;
        this.startTime = System.currentTimeMillis();

        if (!Game.getProtectionSystem().canBuild(user, user.getEyeLocation())) {
            return false;
        }

        removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsOfflineRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user),
                new SwappedSlotsRemovalPolicy<>(user, FireBlast.class)
        );

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (isCharging()) {
            if (!user.isSneaking()) {
                return UpdateResult.Remove;
            }

            if (!Game.getProtectionSystem().canBuild(user, user.getEyeLocation())) {
                return UpdateResult.Remove;
            }

            return UpdateResult.Continue;
        }

        if (!isLaunched()) {
            if (user.isSneaking()) {
                if (!Game.getProtectionSystem().canBuild(user, user.getEyeLocation())) {
                    return UpdateResult.Remove;
                }

                // Display particles showing that it's ready to fire.
                Vector3D direction = user.getDirection();
                Location displayLocation = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                displayLocation = displayLocation.add(side.scalarMultiply(0.5));

                Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.25f, 0.25f, 0.25f, 0.0f, 3, displayLocation, 257);

                return UpdateResult.Continue;
            } else {
                if (!launch()) {
                    return UpdateResult.Remove;
                }
            }
        }

        Location previous = location;

        location = location.add(direction.scalarMultiply(userConfig.speed));

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        if (location.distanceSquared(origin) >= userConfig.range * userConfig.range) {
            return UpdateResult.Remove;
        }

        double displayRadius = Math.max(userConfig.entityCollisionRadius - 1.0, 1.0);

        for (Block block : WorldUtil.getNearbyBlocks(location, displayRadius)) {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.5f, 0.5f, 0.5f, 0.0f, 5, block.getLocation(), 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.5f, 0.5f, 0.5f, 0.0f, 2, block.getLocation(), 257);
        }

        Sphere collider = new Sphere(location.toVector(), userConfig.entityCollisionRadius);

        CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
            if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                return false;
            }

            LivingEntity living = (LivingEntity) entity;
            living.damage(userConfig.damage, user);
            return false;
        }, true);

        Sphere blockCollider = new Sphere(location.toVector(), 0.5);
        Pair<Boolean, Location> collisionResult = CollisionUtil.handleBlockCollisions(blockCollider, previous, location, true);

        if (collisionResult.getFirst()) {
            // Ignite right where the collision happened.
            FireBlast.igniteBlocks(this.user, collisionResult.getSecond(), userConfig.igniteRadius);
        }

        return collisionResult.getFirst() ? UpdateResult.Remove : UpdateResult.Continue;
    }

    public boolean isCharging() {
        return System.currentTimeMillis() < startTime + userConfig.chargeTime;
    }

    public boolean isLaunched() {
        return launched;
    }

    public boolean launch() {
        launched = true;

        origin = user.getEyeLocation();
        location = origin;
        direction = user.getDirection();

        boolean success = !origin.getBlock().isLiquid();

        if (success) {
            user.setCooldown(this, userConfig.cooldown);
        }

        removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);

        return success;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Collection<Collider> getColliders() {
        if (location == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new Sphere(location.toVector(), userConfig.abilityCollisionRadius, world));
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "FireBlastCharged";
    }

    @Override
    public AbilityDescription getDescription() {
        return Game.getAbilityRegistry().getAbilityByName("FireBlast");
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.CHARGE_TIME)
        public long chargeTime;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.RADIUS)
        public double igniteRadius;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "fireblast", "charged");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(1500);
            chargeTime = abilityNode.getNode("charge-time").getLong(2000);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            damage = abilityNode.getNode("damage").getDouble(4.0);
            range = abilityNode.getNode("range").getDouble(20.0);
            igniteRadius = abilityNode.getNode("ignite-radius").getDouble(2.0);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(3.0);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(3.0);
        }
    }
}
