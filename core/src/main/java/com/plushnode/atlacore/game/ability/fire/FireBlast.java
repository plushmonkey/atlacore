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
import com.plushnode.atlacore.game.ability.common.Burstable;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.policies.removal.OutOfWorldRemovalPolicy;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.Collections;

public class FireBlast implements Ability, Burstable {
    public static Config config = new Config();

    private User user;
    private World world;
    private Location origin;
    private Location location;
    private Vector3D direction;
    private CompositeRemovalPolicy removalPolicy;
    private long nextRenderTime;
    private long renderInterval;
    private int particleCount;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.world = user.getWorld();
        this.particleCount = 6;

        if (!Game.getProtectionSystem().canBuild(user, user.getEyeLocation())) {
            return false;
        }

        removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsOfflineRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user)
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
        long time = System.currentTimeMillis();

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

        if (time > this.nextRenderTime) {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.275f, 0.275f, 0.275f, 0.0f, particleCount, location, 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.3f, 0.3f, 0.3f, 0.0f, particleCount / 2, location, 257);

            this.nextRenderTime = time + renderInterval;
        }

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

        Sphere blockCollider = new Sphere(location.toVector(), 0.5);
        Pair<Boolean, Location> collisionResult = CollisionUtil.handleBlockCollisions(blockCollider, previous, location, true);

        if (collisionResult.getFirst()) {
            // Ignite right where the collision happened.
            igniteBlocks(this.user, collisionResult.getSecond());
        }

        return collisionResult.getFirst() ? UpdateResult.Remove : UpdateResult.Continue;
    }

    public static void igniteBlocks(User user, Location location) {
        // The top is used so the ray casted won't collide when going down a layer.
        Location top = location.getBlock().getLocation().add(0.5, 0.95, 0.5);
        Location center = location.getBlock().getLocation().add(0.5, 0.5, 0.5);
        final double NearbyDistSq = 1.5;

        for (Block block : WorldUtil.getNearbyBlocks(center, config.igniteRadius)) {
            if (block.isLiquid()) continue;

            if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                continue;
            }

            // Don't create fire where the user that activated it is standing.
            if (block.getLocation().add(0.5, 0.5, 0.5).distanceSquared(user.getLocation()) < NearbyDistSq) {
                continue;
            }

            if (MaterialUtil.isIgnitable(block)) {
                Location checkTop = block.getLocation().add(0.5, 0.95, 0.5);

                if (!checkTop.getBlock().equals(top.getBlock())) {
                    // Cast a ray to see if there's line of sight of the target block to ignite.
                    // This stops it from igniting blocks through walls.
                    Ray ray = new Ray(top.toVector(), checkTop.subtract(top).toVector().normalize());
                    Location rayLocation = RayCaster.cast(location.getWorld(), ray, config.igniteRadius + 2.0, true);

                    if (rayLocation.distanceSquared(top) < checkTop.distanceSquared(top)) {
                        // The casted ray collided with something before it reached the target ignite block.
                        continue;
                    }
                }

                if (block.getType() != Material.FIRE) {
                    // todo: TempFire
                    new TempBlock(block, Material.FIRE, true, 10000);
                }
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
        return Collections.singletonList(new Sphere(location.toVector(), config.abilityCollisionRadius, world));
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

    @Override
    // Used to initialize the blast for bursts.
    public void initialize(User user, Location location, Vector3D direction) {
        this.user = user;
        this.direction = direction;
        this.origin = location;
        this.location = location;

        removalPolicy = new CompositeRemovalPolicy(getDescription());
    }

    @Override
    public void setRenderInterval(long interval) {
        this.renderInterval = interval;
    }

    @Override
    public void setRenderParticleCount(int count) {
        this.particleCount = count;
    }

    public static class Config extends Configurable {
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
