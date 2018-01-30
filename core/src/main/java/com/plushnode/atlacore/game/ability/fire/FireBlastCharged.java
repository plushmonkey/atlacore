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
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.policies.removal.SwappedSlotsRemovalPolicy;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class FireBlastCharged implements Ability {
    public static Config config = new Config();

    private User user;
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
        this.launched = false;
        this.startTime = System.currentTimeMillis();

        if (!Game.getProtectionSystem().canBuild(user, user.getEyeLocation())) {
            return false;
        }

        removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsOfflineRemovalPolicy(user),
                new SwappedSlotsRemovalPolicy<>(user, FireBlast.class)
        );

        return true;
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

                Vector3D side = direction.crossProduct(Vector3D.PLUS_J).normalize();
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

        location = location.add(direction.scalarMultiply(config.speed));

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        if (location.distanceSquared(origin) >= config.range * config.range) {
            return UpdateResult.Remove;
        }

        double displayRadius = Math.max(config.entityCollisionRadius - 1.0, 1.0);

        for (Block block : WorldUtil.getNearbyBlocks(location, displayRadius)) {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.5f, 0.5f, 0.5f, 0.0f, 5, block.getLocation(), 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.5f, 0.5f, 0.5f, 0.0f, 2, block.getLocation(), 257);
        }

        Sphere collider = new Sphere(location.toVector(), config.entityCollisionRadius);

        CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
            if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                return false;
            }

            LivingEntity living = (LivingEntity) entity;
            living.damage(config.damage, user);
            return false;
        }, true);

        boolean collision = CollisionUtil.handleBlockCollisions(user.getWorld(),
                new Sphere(location.toVector(), 1.0), previous, location, true);

        return collision ? UpdateResult.Remove : UpdateResult.Continue;
    }

    public boolean isCharging() {
        return System.currentTimeMillis() < startTime + config.chargeTime;
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
            user.setCooldown(this);
        }

        removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);

        return success;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(new Sphere(location.toVector(), config.abilityCollisionRadius));
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

    private static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public long chargeTime;
        public double speed;
        public double damage;
        public double range;
        public double entityCollisionRadius;
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

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(3.0);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(3.0);
        }
    }
}
