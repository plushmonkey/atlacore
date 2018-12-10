package com.plushnode.atlacore.game.ability.water.torrent;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.source.SourceType;
import com.plushnode.atlacore.game.ability.common.source.SourceTypes;
import com.plushnode.atlacore.game.ability.common.source.SourceUtil;
import com.plushnode.atlacore.game.ability.water.util.BottleReturn;
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
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Torrent implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state;
    private boolean usedBottle;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (method == ActivationMethod.Punch) {
            SourceTypes sourceTypes = SourceTypes.of(SourceType.Water).and(SourceType.Ice).and(SourceType.Plant);
            List<Torrent> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, Torrent.class);

            if (!instances.isEmpty()) {
                Torrent instance = instances.get(0);

                if (instance.state instanceof SourceState) {
                    Optional<Block> source = SourceUtil.getSource(user, userConfig.selectRange, sourceTypes);

                    if (source.isPresent()) {
                        if (!Game.getProtectionSystem().canBuild(user, source.get().getLocation())) {
                            return false;
                        }
                        instance.createSourceState(source.get());
                        return false;
                    }
                }

                instance.state.onPunch();
                return false;
            }

            Optional<Block> source = SourceUtil.getSource(user, userConfig.selectRange, sourceTypes);

            if (source.isPresent()) {
                if (!Game.getProtectionSystem().canBuild(user, source.get().getLocation())) {
                    return false;
                }

                this.state = new SourceState(source.get());
                return true;
            }
        } else {
            List<Torrent> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, Torrent.class);

            if (!instances.isEmpty()) {
                Torrent instance = instances.get(0);

                instance.state.onSneak();
                return false;
            }

            if (SourceUtil.emptyBottle(user)) {
                this.state = new SwirlState();
                this.usedBottle = true;
                return true;
            }
        }

        return false;
    }

    // State transitions must happen within the correct context so the inner class is correct.
    private void createSourceState(Block sourceBlock) {
        this.state = new SourceState(sourceBlock);
    }

    @Override
    public UpdateResult update() {
        if (!this.state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        if (usedBottle) {
            if (state instanceof SwirlState) {
                SourceUtil.fillBottle(user);
            } else if (state instanceof TravelState) {
                BottleReturn bottleReturn = new BottleReturn(((TravelState)state).location);

                if (bottleReturn.activate(user, ActivationMethod.Punch)) {
                    Game.getAbilityInstanceManager().addAbility(user, bottleReturn);
                }
            }
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Torrent";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    interface State {
        boolean update();
        void onPunch();
        void onSneak();
    }

    private class SourceState implements State {
        private Block sourceBlock;
        private long startTime;

        SourceState(Block sourceBlock) {
            this.sourceBlock = sourceBlock;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public boolean update() {
            if (!user.getWorld().equals(sourceBlock.getLocation().getWorld())) {
                return false;
            }

            if (user.getLocation().distanceSquared(sourceBlock.getLocation()) > userConfig.selectMaxDistance * userConfig.selectMaxDistance) {
                return false;
            }

            if (user.getSelectedAbility() != getDescription()) {
                return false;
            }

            Location renderLocation = this.sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return System.currentTimeMillis() < startTime + userConfig.selectTimeout;
        }

        @Override
        public void onPunch() {

        }

        @Override
        public void onSneak() {
            state = new SourceTravelState(sourceBlock.getLocation().add(0.5, 1, 0.5));
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

        @Override
        public void onPunch() {

        }

        @Override
        public void onSneak() {

        }
    }

    private class SwirlState implements State {
        private static final double DISTANCE_AWAY = 3.0;
        private static final double ANGLE_INCREMENT = 360.0 / (8.0 * DISTANCE_AWAY);
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
                if (trailSize >= MAX_TRAIL_SIZE) {
                    TorrentWave wave = new TorrentWave();

                    if (wave.activate(user, ActivationMethod.Sneak)) {
                        Game.getAbilityInstanceManager().addAbility(user, wave);
                    }
                }
                clear();
                return false;
            }

            return true;
        }

        private void clear() {
            trail.forEach(TempBlock::reset);
            trail.clear();
        }

        @Override
        public void onPunch() {
            if (this.trailSize >= MAX_TRAIL_SIZE && !user.isOnCooldown(getDescription())) {
                List<Block> swirl = trail.stream()
                        .map(tb -> tb.getPreviousState().getBlock())
                        .collect(Collectors.toList());

                state = new TravelState(swirl);
                clear();
            }
        }

        @Override
        public void onSneak() {

        }

        protected List<Block> getSwirl(Vector3D direction, double angle, int trailSize) {
            List<Block> trail = new ArrayList<>();

            for (int i = 0; i < trailSize; ++i) {
                double currentRads = Math.toRadians(angle + (i * ANGLE_INCREMENT));
                Vector3D offset = VectorUtil.rotate(direction, Vector3D.PLUS_J, currentRads).scalarMultiply(DISTANCE_AWAY);
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

    private class TravelState implements State {
        private List<TempBlock> tempBlocks = new ArrayList<>();
        private List<Block> swirl;
        private Location origin;
        private Location location;
        private Vector3D direction;
        private double tailSize;
        private double movementBuffer;
        private boolean freezing;
        private List<Entity> affectedEntities = new ArrayList<>();

        TravelState(List<Block> swirl) {
            this.swirl = new ArrayList<>(swirl);
            this.tailSize = 0.0;
            this.movementBuffer = 0.0;
            this.freezing = false;
            this.origin = user.getEyeLocation();
            this.location = swirl.get(0).getLocation().add(0.5, 0.5, 0.5);

            user.setCooldown(Torrent.this, userConfig.cooldown);

            Location target = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.range + userConfig.speed));
            this.direction = target.subtract(location).toVector().normalize();
        }

        @Override
        public boolean update() {
            // Implemented this way to make it more robust to differing speeds.
            this.movementBuffer += userConfig.speed;

            Location previous = location;

            while (this.movementBuffer > 1.0) {
                if (!swirl.isEmpty()) {
                    popSwirl();
                }
                location = location.add(direction);
                this.movementBuffer -= 1.0;
            }

            clear();

            if (location.distanceSquared(origin) > userConfig.range * userConfig.range) {
                return remove();
            }

            Pair<Boolean, Location> collision = CollisionUtil.handleBlockCollisions(new Sphere(previous, userConfig.speed), previous, location, false);

            if (collision.getFirst()) {
                location = collision.getSecond();
                return remove();
            }


            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            render();

            boolean activateFreeze = false;
            for (TempBlock tb : tempBlocks) {
                Location location = tb.getPreviousState().getLocation();

                AABB collider = AABB.BLOCK_BOUNDS.scale(userConfig.entityCollisionRadius * 2).at(location);
                boolean hit = CollisionUtil.handleEntityCollisions(user, collider, entity -> {
                    if (!affectedEntities.contains(entity)) {
                        ((LivingEntity)entity).damage(userConfig.damage);

                        if (!freezing) {
                            Vector3D knockback = direction;

                            if (knockback.getY() > userConfig.verticalPush) {
                                knockback = VectorUtil.setY(knockback, userConfig.verticalPush);
                            }

                            entity.setVelocity(knockback.scalarMultiply(userConfig.knockback));
                        }

                        affectedEntities.add(entity);
                    }
                    return false;
                }, true);

                if (hit) {
                    activateFreeze = true;
                }
            }

            if (freezing && activateFreeze) {
                return remove();
            }

            tailSize += Math.min(userConfig.speed, swirl.size());

            return true;
        }

        private boolean remove() {
            clear();

            if (freezing) {
                double extent = userConfig.freezeRadius + 1;
                Collection<Entity> nearby = location.getWorld().getNearbyEntities(location, extent, extent, extent);

                for (Block block : WorldUtil.getNearbyBlocks(location, userConfig.freezeRadius)) {
                    if (MaterialUtil.isTransparent(block) || block.getType() == Material.WATER) {
                        boolean canFreeze = true;

                        for (Entity entity : nearby) {
                            if (!(entity instanceof LivingEntity)) continue;

                            // Don't add ice when an entity's head is there.
                            Vector3D eye = ((LivingEntity)entity).getEyeLocation().toVector();
                            if (AABB.BLOCK_BOUNDS.at(block.getLocation()).contains(eye)) {
                                canFreeze = false;
                                break;
                            }
                        }

                        if (canFreeze) {
                            new TempBlock(block, Material.ICE, userConfig.freezeDuration);
                        }
                    }
                }
            }

            return false;
        }

        private void clear() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();
        }

        private void render() {
            // Render the remaining swirl
            swirl.forEach(block -> {
                tempBlocks.add(new TempBlock(block, Material.WATER));
            });

            // Render the tail
            for (int i = 0; i < this.tailSize; ++i) {
                Block block = location.subtract(direction.scalarMultiply(i)).getBlock();
                tempBlocks.add(new TempBlock(block, Material.WATER));
            }

            // Render the head
            tempBlocks.add(new TempBlock(location.getBlock(), Material.WATER));
        }

        private Block popSwirl() {
            Block result = swirl.remove(swirl.size() - 1);

            if (result.getType() == Material.WATER) {
                Game.getTempBlockService().reset(result);
            }

            return result;
        }

        @Override
        public void onPunch() {
            this.freezing = true;
        }

        @Override
        public void onSneak() {

        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.SELECTION)
        public double selectMaxDistance;
        public long selectTimeout;
        @Attribute(Attributes.SPEED)
        public double sourceTravelSpeed;
        public int swirlIncrease;
        @Attribute(Attributes.CHARGE_TIME)
        public double swirlSpeed;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.STRENGTH)
        public double knockback;
        @Attribute(Attributes.STRENGTH)
        public double verticalPush;
        @Attribute(Attributes.RADIUS)
        public double freezeRadius;
        @Attribute(Attributes.DURATION)
        public int freezeDuration;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "torrent");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            selectRange = abilityNode.getNode("select-range").getDouble(10.0);
            selectMaxDistance = abilityNode.getNode("select-max-distance").getDouble(30.0);
            selectTimeout = abilityNode.getNode("select-timeout").getLong(30000);
            sourceTravelSpeed = abilityNode.getNode("source-travel-speed").getDouble(1.0);
            swirlSpeed = abilityNode.getNode("swirl-speed").getDouble(3.0);
            swirlIncrease = abilityNode.getNode("swirl-increase").getInt(4);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            range = abilityNode.getNode("range").getDouble(25.0);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            knockback = abilityNode.getNode("knockback").getDouble(1.0);
            verticalPush = abilityNode.getNode("vertical-push").getDouble(0.2);
            freezeRadius = abilityNode.getNode("freeze-radius").getDouble(3.0);
            freezeDuration = abilityNode.getNode("freeze-duration").getInt(10000);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
        }
    }
}
