package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.source.SourceType;
import com.plushnode.atlacore.game.ability.common.source.SourceTypes;
import com.plushnode.atlacore.game.ability.common.source.SourceUtil;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// TODO: This reuses a lot of things from Torrent. They should probably be collapsed eventually.
// TODO: Ice
// TODO: Display cooldown on board
public class WaterSpoutWave implements Ability {
    private static final double SWIRL_DISTANCE = 3.0;

    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state;
    private boolean iceWave;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.iceWave = false;

        recalculateConfig();

        if (!userConfig.enabled) {
            return false;
        }

        Collection<WaterSpoutWave> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, WaterSpoutWave.class);
        if (!instances.isEmpty()) {
            instances.iterator().next().state.onPunch();
            return false;
        }

        if (user.isOnCooldown(getDescription())) {
            return false;
        }

        SourceTypes sourceTypes = SourceTypes.of(SourceType.Water).and(SourceType.Ice).and(SourceType.Plant);
        Optional<Block> source = SourceUtil.getSource(user, userConfig.selectRange, sourceTypes);

        if (!source.isPresent()) {
            return false;
        }

        this.state = new SourceState(source.get());

        return true;
    }

    @Override
    public UpdateResult update() {
        if (!state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    public boolean isActive() {
        return !(this.state instanceof SourceState);
    }

    public boolean freeze() {
        boolean result = !this.iceWave;

        this.iceWave = true;

        return result;
    }

    @Override
    public void destroy() {

    }

    private interface State {
        boolean update();
        default void onPunch() { }
    }

    private class SourceState implements State {
        private Block sourceBlock;

        SourceState(Block sourceBlock) {
            this.sourceBlock = sourceBlock;
        }

        @Override
        public boolean update() {
            if (!user.getWorld().equals(sourceBlock.getLocation().getWorld())) {
                return false;
            }

            if (user.getLocation().distanceSquared(sourceBlock.getLocation()) > userConfig.selectMaxDistance * userConfig.selectMaxDistance) {
                return false;
            }

            AbilityDescription desc = user.getSelectedAbility();

            if (desc == null || !"WaterSpout".equals(desc.getName())) {
                return false;
            }

            if (user.isSneaking()) {
                state = new SourceTravelState(sourceBlock.getLocation().add(0.5, 0.5, 0.5));
            }

            Location renderLocation = this.sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return true;
        }
    }

    // This state occurs when the water is traveling from the source location to the player.
    // It transitions to SwirlState after reaching the player.
    private class SourceTravelState implements State {
        private Location location;
        private TempBlock tempBlock;

        SourceTravelState(Location origin) {
            this.location = origin;
            this.tempBlock = null;
        }

        @Override
        public boolean update() {
            clear();

            Location target = user.getEyeLocation();

            if (!target.getWorld().equals(location.getWorld())) {
                return false;
            }

            double distSq = target.distanceSquared(location);
            final double maxDist = 50;

            if (distSq > maxDist * maxDist) {
                return false;
            }

            if (distSq <= userConfig.sourceTravelSpeed * userConfig.sourceTravelSpeed) {
                state = new SwirlState();
                return true;
            }

            Vector3D direction = target.subtract(location).toVector().normalize();
            location = location.add(direction.scalarMultiply(userConfig.sourceTravelSpeed));

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            Block block = location.getBlock();

            if (MaterialUtil.isTransparent(block)) {
                this.tempBlock = new TempBlock(location.getBlock(), Material.WATER);
            }

            return true;
        }

        private void clear() {
            if (this.tempBlock != null) {
                this.tempBlock.reset();
                this.tempBlock = null;
            }
        }
    }

    private class SwirlState implements State {
        private static final double ANGLE_INCREMENT = 360.0 / (8.0 * SWIRL_DISTANCE);
        private static final int MAX_TRAIL_SIZE = 12;

        private double angle;
        private Vector3D direction;
        private int trailSize;
        private List<TempBlock> trail = new ArrayList<>();

        SwirlState() {
            this.angle = 360;
            this.trailSize = 1;
            this.direction = getDirection();
        }

        @Override
        public boolean update() {
            clear();

            this.angle -= ANGLE_INCREMENT * userConfig.swirlSpeed;

            if (this.angle <= 0) {
                this.angle += 360;
                trailSize = Math.min(trailSize + userConfig.swirlIncrease, MAX_TRAIL_SIZE);
            }

            for(Block block : getSwirl(this.direction, angle, trailSize)) {
                trail.add(new TempBlock(block, Material.WATER));
            }

            if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
                return false;
            }

            if (!user.isSneaking()) {
                clear();

                if (trailSize >= MAX_TRAIL_SIZE) {
                    state = new SwirlCollapseState();

                    return true;
                }
                return false;
            }

            return true;
        }

        private void clear() {
            trail.forEach(TempBlock::reset);
            trail.clear();
        }

        protected List<Block> getSwirl(Vector3D direction, double angle, int trailSize) {
            List<Block> trail = new ArrayList<>();

            for (int i = 0; i < trailSize; ++i) {
                double currentRads = Math.toRadians(angle + (i * ANGLE_INCREMENT));
                Vector3D offset = VectorUtil.rotate(direction, Vector3D.PLUS_J, currentRads).scalarMultiply(SWIRL_DISTANCE);
                Location location = user.getEyeLocation().add(offset);
                Block block = location.getBlock();

                if (MaterialUtil.isTransparent(block)) {
                    trail.add(block);
                }
            }

            return trail;
        }

        protected Vector3D getDirection() {
            return VectorUtil.normalizeOrElse(VectorUtil.setY(user.getDirection(), 0), Vector3D.PLUS_I);
        }
    }

    private class SwirlCollapseState implements State {
        private List<TempBlock> tempBlocks;
        private double radius;

        SwirlCollapseState() {
            this.radius = SWIRL_DISTANCE;
            this.tempBlocks = new ArrayList<>();
        }

        @Override
        public boolean update() {
            this.radius -= userConfig.swirlCollapseSpeed;

            for (TempBlock tempBlock : tempBlocks) {
                tempBlock.reset();
            }

            tempBlocks.clear();

            if (this.radius <= 0.0) {
                state = new WaveTravelState();
                return true;
            }

            for (double theta = 0; theta < 2 * Math.PI; theta += ((Math.PI * 2) / (8.0 * radius))) {
                double x = Math.cos(theta) * radius;
                double z = Math.sin(theta) * radius;

                Location location = user.getEyeLocation().add(x, 0, z);
                Block block = location.getBlock();

                if (MaterialUtil.isTransparent(block) && Game.getProtectionSystem().canBuild(user, location)) {
                    tempBlocks.add(new TempBlock(block, Material.WATER));
                }
            }

            return true;
        }
    }

    private class WaveTravelState implements State {
        private List<FutureBlock> futureBlocks = new ArrayList<>();
        private List<Entity> affectedEntities = new ArrayList<>();
        private long startTime;
        private boolean active;

        WaveTravelState() {
            this.startTime = System.currentTimeMillis();
            this.active = true;
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();

            if (time - startTime > userConfig.travelDuration || !this.active) {
                return remove();
            }

            if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
                return remove();
            }

            Vector3D direction = user.getDirection();

            // Shrink the speed over time.
            double t = 1.0 - ((time - startTime) / (double)userConfig.travelDuration);
            double speed = userConfig.travelSpeed * t;

            user.setVelocity(direction.scalarMultiply(speed));
            user.setFallDistance(0);

            Location center = user.getLocation().subtract(0, 1, 0);
            Material material = iceWave ? Material.ICE : Material.WATER;

            for (Block block : WorldUtil.getNearbyBlocks(center, userConfig.waveRadius, Material.WATER, Material.ICE)) {
                if (MaterialUtil.isTransparent(block)) {
                    long futureTime = 0;

                    if (iceWave) {
                        futureTime = time + 100;
                    }

                    futureBlocks.add(new FutureBlock(block, material, futureTime));
                }
            }

            // Render the ice in the future so it doesn't obstruct the traveling player. Water gets rendered immediately.
            futureBlocks.removeIf(futureBlock -> {
               if (time < futureBlock.time) {
                   return false;
               }

               if (futureBlock.block.getType() == futureBlock.material) {
                   return true;
               }

               new TempBlock(futureBlock.block, futureBlock.material, 1000);

               return true;
            });

            if (iceWave) {
                Sphere collider = new Sphere(user.getLocation(), userConfig.entityCollisionRadius);

                CollisionUtil.handleEntityCollisions(user, collider, entity -> {
                    if (affectedEntities.contains(entity)) return false;

                    if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                        return false;
                    }

                    affectedEntities.add(entity);
                    ((LivingEntity)entity).damage(userConfig.iceDamage);
                    createIceSphere(entity.getLocation());

                    return false;
                }, true);
            }

            return true;
        }

        private boolean remove() {
            user.setCooldown(WaterSpoutWave.this, userConfig.cooldown);
            return false;
        }

        @Override
        public void onPunch() {
            this.active = false;
        }

        private void createIceSphere(Location location) {
            for (Block block : WorldUtil.getNearbyBlocks(location, userConfig.iceSphereRadius, Material.ICE)) {
                if (MaterialUtil.isTransparent(block) && Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                    new TempBlock(block, Material.ICE, userConfig.iceSphereDuration);
                }
            }
        }
    }

    private static class FutureBlock {
        Block block;
        Material material;
        long time;

        FutureBlock(Block block, Material material, long time) {
            this.block = block;
            this.material = material;
            this.time = time;
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterSpoutWave";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.SELECTION)
        public double selectMaxDistance;
        public double sourceTravelSpeed;
        public int swirlIncrease;
        @Attribute(Attributes.CHARGE_TIME)
        public double swirlSpeed;
        @Attribute(Attributes.CHARGE_TIME)
        public double swirlCollapseSpeed;
        @Attribute(Attributes.RADIUS)
        public double waveRadius;
        @Attribute(Attributes.SPEED)
        public double travelSpeed;
        public long travelDuration;
        @Attribute(Attributes.STRENGTH)
        public double iceSphereRadius;
        @Attribute(Attributes.DAMAGE)
        public double iceDamage;
        @Attribute(Attributes.DURATION)
        public long iceSphereDuration;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterspout", "wave");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            selectRange = abilityNode.getNode("select-range").getDouble(6.0);
            selectMaxDistance = abilityNode.getNode("select-max-distance").getDouble(10.0);
            sourceTravelSpeed = abilityNode.getNode("source-travel-speed").getDouble(1.0);
            swirlIncrease = abilityNode.getNode("swirl", "increase").getInt(12);
            swirlSpeed = abilityNode.getNode("swirl", "speed").getDouble(3.0);
            swirlCollapseSpeed = abilityNode.getNode("swirl", "collapse-speed").getDouble(0.2);
            waveRadius = abilityNode.getNode("radius").getDouble(1.5);
            travelSpeed = abilityNode.getNode("travel-speed").getDouble(1.3);
            travelDuration = abilityNode.getNode("travel-duration").getLong(2500);
            entityCollisionRadius = abilityNode.getNode("ice").getNode("entity-collision-radius").getDouble(2.5);
            iceSphereRadius = abilityNode.getNode("ice").getNode("sphere-radius").getDouble(2.5);
            iceDamage = abilityNode.getNode("ice").getNode("damage").getDouble(3.0);
            iceSphereDuration = abilityNode.getNode("ice").getNode("duration").getLong(4000);
        }
    }
}
