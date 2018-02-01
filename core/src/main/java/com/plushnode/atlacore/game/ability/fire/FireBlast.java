package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.block.TempBlock;
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
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class FireBlast implements Ability {
    public static Config config = new Config();

    private User user;
    private Location origin;
    private Location location;
    private Vector3D direction;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        if (!Game.getProtectionSystem().canBuild(user, user.getEyeLocation())) {
            return false;
        }

        removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsOfflineRemovalPolicy(user)
        );

        if (method == ActivationMethod.Punch) {
            for (FireBlastCharged charged : Game.getAbilityInstanceManager().getPlayerInstances(user, FireBlastCharged.class)) {
                if (charged.isCharging()) {
                    return false;
                }

                if (!charged.isLaunched()) {
                    if (!charged.launch()) {
                        Game.getAbilityInstanceManager().destroyInstance(user, charged);
                    }
                    return false;
                }
            }

            return launch();
        } else if (method == ActivationMethod.Sneak) {
            FireBlastCharged charged = new FireBlastCharged();

            charged.activate(user, method);

            Game.getAbilityInstanceManager().addAbility(user, charged);

            // This should return false so the ability activator doesn't add this to active instances list.
            return false;
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        Location previous = location;

        location = location.add(direction.scalarMultiply(config.speed));

        if (location.distanceSquared(origin) >= config.range * config.range) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.275f, 0.275f, 0.275f, 0.0f, 6, location, 257);
        Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.3f, 0.3f, 0.3f, 0.0f, 3, location, 257);

        Sphere collider = new Sphere(location.toVector(), config.entityCollisionRadius);

        boolean hit = CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
            if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                return false;
            }

            LivingEntity living = (LivingEntity) entity;
            living.damage(config.damage, user);
            return true;
        }, true);

        if (hit) {
            return UpdateResult.Remove;
        }

        boolean collision = CollisionUtil.handleBlockCollisions(user.getWorld(),
                new Sphere(location.toVector(), 1.0), previous, location, true);

        if (collision) {
            igniteBlocks();
        }

        return collision ? UpdateResult.Remove : UpdateResult.Continue;
    }

    private void igniteBlocks() {
        // Get the top of the block right before the collision happened.
        // The top is used so the ray casted won't collide when going down a layer.
        Location top = location.subtract(direction.scalarMultiply(config.speed)).getBlock().getLocation().add(0.5, 0.95, 0.5);

        for (Block block : WorldUtil.getNearbyBlocks(location, config.igniteRadius)) {
            if (block.isLiquid()) continue;

            if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                continue;
            }

            if (IgnitableBlocks.isIgnitable(block)) {
                Location checkTop = block.getLocation().add(0.5, 0.95, 0.5);

                if (!checkTop.getBlock().equals(top.getBlock())) {
                    // Cast a ray to see if there's line of sight of the target block to ignite.
                    // This stops it from igniting blocks through walls.
                    Ray ray = new Ray(top.toVector(), checkTop.subtract(top).toVector().normalize());
                    Location rayLocation = RayCaster.cast(location.getWorld(), ray, config.igniteRadius, true);

                    if (rayLocation.distanceSquared(top) < checkTop.distanceSquared(top)) {
                        // The casted ray collided with something before it reached the target ignite block.
                        continue;
                    }
                }

                // todo: TempFire
                new TempBlock(block, Material.FIRE);
            }
        }
    }

    @Override
    public void destroy() {

    }

    private boolean launch() {
        origin = user.getEyeLocation();
        location = user.getEyeLocation();
        direction = user.getDirection();

        boolean success = !origin.getBlock().isLiquid();

        if (success) {
            user.setCooldown(this);
        }

        return success;
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
        return "FireBlast";
    }

    private static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double speed;
        public double damage;
        public double range;
        public double igniteRadius;
        public double entityCollisionRadius;
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "fireblast");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(1500);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            range = abilityNode.getNode("range").getDouble(25.0);
            igniteRadius = abilityNode.getNode("ignite-radius").getDouble(2.0);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(2.0);
        }
    }
}
