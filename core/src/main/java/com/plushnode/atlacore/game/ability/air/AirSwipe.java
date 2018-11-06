package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.policies.removal.OutOfWorldRemovalPolicy;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsDeadRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

public class AirSwipe implements Ability {
    public static Config config = new Config();

    private User user;
    private World world;
    private Location origin;
    private List<AirStream> streams = new ArrayList<>();
    private CompositeRemovalPolicy removalPolicy;
    private boolean charging;
    private long startTime;
    private double factor = 1.0;
    private List<Entity> affectedEntities = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.world = user.getWorld();
        this.origin = user.getEyeLocation();
        this.startTime = System.currentTimeMillis();
        this.charging = true;

        if (user.getEyeLocation().getBlock().isLiquid()) {
            return false;
        }

        removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new IsOfflineRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user)
        );

        for (AirSwipe swipe : Game.getAbilityInstanceManager().getPlayerInstances(user, AirSwipe.class)) {
            if (swipe.charging) {
                swipe.launch();
                return false;
            }
        }

        if (!Game.getProtectionSystem().canBuild(user, origin)) {
            return false;
        }

        if (method == ActivationMethod.Punch) {
            launch();
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        long time = System.currentTimeMillis();

        affectedEntities.clear();

        if (charging) {
            // Make sure the user keeps this ability selected while charging.
            if (user.getSelectedAbility() != getDescription()) {
                return UpdateResult.Remove;
            }

            if (user.isSneaking() && time >= startTime + config.maxChargeTime) {
                Vector3D direction = user.getDirection();
                Location location = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                location = location.add(side.scalarMultiply(0.5));

                // Display air particles to the right of the player.
                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
            } else if (!user.isSneaking()) {
                factor = Math.max(1.0, Math.min(1.0, (time - startTime) / (double)config.maxChargeTime) * config.chargeFactor);

                launch();
            }
        } else {
            for (Iterator<AirStream> iterator = streams.iterator(); iterator.hasNext(); ) {
                AirStream stream = iterator.next();

                if (!stream.update()) {
                    iterator.remove();
                }
            }
        }

        return (charging || !streams.isEmpty()) ? UpdateResult.Continue : UpdateResult.Remove;
    }

    @Override
    public void destroy() {

    }

    private void launch() {
        charging = false;

        user.setCooldown(this);

        origin = user.getEyeLocation();

        Vector3D up = Vector3D.PLUS_J;
        Vector3D lookingDir = user.getDirection();
        Vector3D right = VectorUtil.normalizeOrElse(lookingDir.crossProduct(up), Vector3D.PLUS_I);
        Vector3D rotateAxis = right.crossProduct(lookingDir);

        double halfArc = config.arc / 2;

        for (double deg = -halfArc; deg <= halfArc; deg += config.arcStep) {
            double rads = Math.toRadians(deg);

            Vector3D direction = user.getDirection();
            direction = VectorUtil.rotate(direction, rotateAxis, rads);

            streams.add(new AirStream(origin, direction));
        }
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    @Override
    public Collection<Collider> getColliders() {
        return streams.stream()
                .map((stream) -> new Sphere(stream.location.toVector(), config.abilityCollisionRadius, world))
                .collect(Collectors.toList());
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirSwipe";
    }

    private class AirStream {
        private Location location;
        private Vector3D direction;

        AirStream(Location origin, Vector3D direction) {
            this.location = origin;
            this.direction = direction;
        }

        // Return false to destroy this stream
        boolean update() {
            Location previous = location;
            location = location.add(direction.scalarMultiply(config.speed));

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            if (location.distanceSquared(origin) >= config.range * config.range) {
                return false;
            }

            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);

            Collider collider = new Sphere(location.toVector(), config.entityCollisionRadius);
            if (collide(collider)) {
                return false;
            }

            for (Block handleBlock : WorldUtil.getNearbyBlocks(location, 2.0)) {
                if (!handleBlockInteractions(handleBlock)) {
                    return false;
                }
            }

            return !CollisionUtil.handleBlockCollisions(new Sphere(location.toVector(), 0.5), previous, location, true).getFirst();
        }

        private boolean handleBlockInteractions(Block block) {
            if (block.isLiquid()) {
                if (MaterialUtil.isLava(block)) {
                    // TODO: Check if source block using BlockData.
                    if (block.getType() == Material.LAVA) {
                        block.setType(Material.OBSIDIAN);
                    } else {
                        block.setType(Material.COBBLESTONE);
                    }
                }
            }

            if (block.getType() == Material.FIRE) {
                block.setType(Material.AIR);
            }

            return true;
        }

        // Returns true if it collides with an entity
        private boolean collide(Collider collider) {
            return CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    return false;
                }

                entity.setVelocity(direction.scalarMultiply(factor));

                if (entity instanceof LivingEntity && !affectedEntities.contains(entity)) {
                    ((LivingEntity) entity).damage(config.damage * factor, user);
                    affectedEntities.add(entity);
                    return false;
                }

                return false;
            }, false);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double damage;
        public double range;
        public double speed;
        public int arc;
        public int arcStep;
        public long maxChargeTime;
        public double chargeFactor;
        public double entityCollisionRadius;
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airswipe");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(1500);
            damage = abilityNode.getNode("damage").getDouble(2.0);
            range = abilityNode.getNode("range").getDouble(14.0);
            arc = abilityNode.getNode("arc").getInt(32);
            arcStep = abilityNode.getNode("arc-step").getInt(4);
            speed = abilityNode.getNode("speed").getDouble(1.25);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(0.5);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(0.5);
            maxChargeTime = abilityNode.getNode("charge").getNode("max-time").getLong(2500);
            chargeFactor = abilityNode.getNode("charge").getNode("factor").getDouble(3.0);

            abilityNode.getNode("speed").setComment("How many meters the streams advance with each tick.");
            abilityNode.getNode("arc").setComment("How large the entire arc is in degrees.");
            abilityNode.getNode("arc-step").setComment("How many degrees apart each stream is in the arc.");
            abilityNode.getNode("charge").getNode("max-time").setComment("How many milliseconds it takes to fully charge.");
            abilityNode.getNode("charge").getNode("factor").setComment("How much the damage and knockback are multiplied by at full charge.");
        }
    }
}
