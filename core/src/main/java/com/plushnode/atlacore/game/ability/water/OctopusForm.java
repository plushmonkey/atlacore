package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.source.SourceType;
import com.plushnode.atlacore.game.ability.common.source.SourceTypes;
import com.plushnode.atlacore.game.ability.common.source.SourceUtil;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class OctopusForm implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state;
    private List<TempBlock> frozenBlocks = new ArrayList<>();
    private RemovalPolicy removalPolicy;
    private boolean usedBottle;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        OctopusForm instance = Game.getAbilityInstanceManager().getFirstInstance(user, OctopusForm.class);

        if (method == ActivationMethod.Punch) {
            if (instance != null) {
                // Perform punch action if an existing instance was found.
                instance.state.onPunch();
                return false;
            }

            // Attempt to source a new instance because no others exist.
            SourceTypes sourceTypes = SourceTypes.of(SourceType.Water).and(SourceType.Ice).and(SourceType.Plant);
            Optional<Block> source = SourceUtil.getSource(user, userConfig.selectRange, sourceTypes);

            if (source.isPresent()) {
                if (Game.getProtectionSystem().canBuild(user, source.get().getLocation())) {
                    createRemovalPolicy();
                    this.state = new SourceState(source.get());

                    return true;
                }
            }

            return false;
        } else if (method == ActivationMethod.Sneak) {
            if (instance != null) {
                instance.state.onSneak();
                return false;
            }

            if (SourceUtil.emptyBottle(user)) {
                createRemovalPolicy();
                this.state = new SwirlState();
                this.usedBottle = true;
                return true;
            }
        }

        return false;
    }

    private void createRemovalPolicy() {
        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new IsOfflineRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user)
        );
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove() || !state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        if (this.state != null) {
            this.state.onDestroy();
        }

        user.setCooldown(this);

        for (TempBlock tempBlock : frozenBlocks) {
            if (tempBlock.getBlock().getType() == Material.ICE) {
                tempBlock.reset();
            }
        }

        frozenBlocks.clear();

        if (usedBottle) {
            SourceUtil.fillBottle(user);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "OctopusForm";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    private interface State {
        // Return false to destroy the ability
        boolean update();

        void onPunch();
        void onSneak();
        void onDestroy();
    }

    private class SourceState implements State {
        private Block sourceBlock;
        private Location sourceLocation;

        SourceState(Block sourceBlock) {
            setSourceBlock(sourceBlock);
        }

        @Override
        public boolean update() {
            if (!user.getWorld().equals(sourceBlock.getLocation().getWorld())) {
                return false;
            }

            if (user.getLocation().distanceSquared(sourceLocation) > userConfig.selectMaxDistance * userConfig.selectMaxDistance) {
                return false;
            }

            if (user.getSelectedAbility() != getDescription()) {
                return false;
            }

            Location renderLocation = this.sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return true;
        }

        @Override
        public void onPunch() {
            // Try to find a new block to source.
            SourceTypes sourceTypes = SourceTypes.of(SourceType.Water).and(SourceType.Ice).and(SourceType.Plant);
            Optional<Block> source = SourceUtil.getSource(user, userConfig.selectRange, sourceTypes);

            if (source.isPresent()) {
                if (Game.getProtectionSystem().canBuild(user, source.get().getLocation())) {
                    setSourceBlock(source.get());
                }
            }
        }

        @Override
        public void onSneak() {
            state = new SourceTravelState(sourceLocation);
        }

        @Override
        public void onDestroy() {

        }

        private void setSourceBlock(Block sourceBlock) {
            this.sourceBlock = sourceBlock;
            this.sourceLocation = sourceBlock.getLocation().add(0.5, 0.5, 0.5);
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

            if (!user.isSneaking()) {
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

        @Override
        public void onDestroy() {
            clear();
        }
    }

    private abstract class AbstractRenderState implements State {
        protected List<TempBlock> swirl = new ArrayList<>();

        protected void render(double initialOrientation, double finalOrientation) {
            clear();

            Location base = user.getLocation();
            double delta = Math.toDegrees((Math.PI * 2) / (8 * userConfig.radius));

            for (double angle = initialOrientation; angle < finalOrientation; angle += delta) {
                double theta = Math.toRadians(angle);
                double x = Math.cos(theta) * userConfig.radius;
                double z = Math.sin(theta) * userConfig.radius;

                Location location = base.add(x, 0, z);
                Block block = location.getBlock();

                if (!Game.getProtectionSystem().canBuild(user, location)) {
                    continue;
                }

                if (MaterialUtil.isTransparent(block) || block.getType() == Material.ICE) {
                    if (block.getType() == Material.ICE) {
                        TempBlock tempBlock = Game.getTempBlockService().getTempBlock(block);

                        if (tempBlock != null) {
                            if (frozenBlocks.remove(tempBlock)) {
                                tempBlock.reset();
                            }
                        }
                    }

                    Block below = block.getRelative(BlockFace.DOWN);

                    if (below.getType() == Material.WATER && Game.getProtectionSystem().canBuild(user, location)) {
                        frozenBlocks.add(new TempBlock(below, Material.ICE));
                    }

                    swirl.add(new TempBlock(block, Material.WATER));
                }
            }
        }

        protected void clear() {
            for (TempBlock tempBlock : swirl) {
                tempBlock.reset();
            }
            swirl.clear();
        }

        @Override
        public void onDestroy() {
            clear();
        }
    }

    private class SwirlState extends AbstractRenderState {
        private double initialOrientation;
        private double orientation;
        private double rotation;

        SwirlState() {
            this.initialOrientation = user.getYaw() + 90;
            this.orientation = initialOrientation;
            if (userConfig.chargeTime > 0) {
                this.rotation = 360.0 / (userConfig.chargeTime / 1000.0) / 20.0;
            } else {
                this.rotation = 360.0;
            }
        }

        @Override
        public boolean update() {
            this.orientation += this.rotation;

            if (this.orientation - initialOrientation >= 360.0) {
                clear();

                FormedState newState = new FormedState();
                newState.render();

                state = newState;

                return true;
            }

            if (!user.isSneaking()) {
                clear();
                return false;
            }

            render(initialOrientation, orientation);

            return true;
        }

        @Override
        public void onPunch() {

        }

        @Override
        public void onSneak() {

        }
    }

    private class FormedState extends AbstractRenderState {
        private static final double TENTACLE_COUNT = 8;

        private List<TempBlock> tentacleBlocks = new ArrayList<>();
        private long attackEndTime;

        @Override
        public boolean update() {
            if (!user.isSneaking()) {
                clear();
                return false;
            }

            render();

            return true;
        }

        private void render() {
            long time = System.currentTimeMillis();

            // Render the base swirl
            super.render(0, 360);

            // Render the tentacles
            for (int i = 0; i < TENTACLE_COUNT; ++i) {
                double tTop = (System.currentTimeMillis() + i) * 0.0072;
                double tBottom = (System.currentTimeMillis() + i) * 0.0028;

                // Fluctuate between 0.0 and 1.0 according to the t speed.
                double topOffset = (Math.sin(tTop) + 1.0) / 2.0;
                double bottomOffset = (Math.sin(tBottom) + 1.0) / 2.0;

                // Extend the tentacles outwards during attack animation.
                if (attackEndTime > time) {
                    topOffset = 2.0;
                    bottomOffset = 1.0;
                }

                double theta = Math.toRadians((i / TENTACLE_COUNT) * 360.0) + Math.toRadians(user.getYaw());

                double xBottom = Math.cos(theta) * (userConfig.radius + bottomOffset);
                double zBottom = Math.sin(theta) * (userConfig.radius + bottomOffset);
                Location bottomLocation = user.getLocation().add(xBottom, 1, zBottom);
                Block bottomBlock = bottomLocation.getBlock();

                double xTop = Math.cos(theta) * (userConfig.radius + topOffset);
                double zTop = Math.sin(theta) * (userConfig.radius + topOffset);
                Location topLocation = user.getLocation().add(xTop, 2, zTop);
                Block topBlock = topLocation.getBlock();

                if (MaterialUtil.isTransparent(bottomBlock)) {
                    tentacleBlocks.add(new TempBlock(bottomBlock, Material.WATER));
                }

                if (MaterialUtil.isTransparent(topBlock)) {
                    tentacleBlocks.add(new TempBlock(topBlock, Material.WATER));
                }
            }
        }

        protected void clear() {
            super.clear();

            for (TempBlock tempBlock : tentacleBlocks) {
                tempBlock.reset();
            }
            tentacleBlocks.clear();
        }

        @Override
        public void onPunch() {
            long time = System.currentTimeMillis();

            // Attack
            if (userConfig.attackAnimation && attackEndTime <= time) {
                attackEndTime = System.currentTimeMillis() + 200;
            }

            List<Entity> affected = new ArrayList<>();

            for (int i = 0; i < TENTACLE_COUNT; ++i) {
                double theta = Math.toRadians((i / TENTACLE_COUNT) * 360.0) + Math.toRadians(user.getYaw());

                double x = Math.cos(theta) * (userConfig.radius + 0.5);
                double z = Math.sin(theta) * (userConfig.radius + 0.5);

                Location tentacleBase = user.getLocation().add(x, 0, z);
                AABB hitbox = userConfig.attackHitbox.at(tentacleBase);

                CollisionUtil.handleEntityCollisions(user, hitbox, entity -> {
                    if (affected.contains(entity)) return false;
                    if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) return false;

                    ((LivingEntity)entity).damage(userConfig.attackDamage);

                    Vector3D direction = tentacleBase.subtract(user.getLocation()).toVector().normalize();
                    entity.setVelocity(direction.scalarMultiply(userConfig.attackKnockback));

                    affected.add(entity);

                    return false;
                }, true);
            }
        }

        @Override
        public void onSneak() {

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            clear();
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
        @Attribute(Attributes.SPEED)
        public double sourceTravelSpeed;
        @Attribute(Attributes.CHARGE_TIME)
        public long chargeTime;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.DAMAGE)
        public double attackDamage;
        @Attribute(Attributes.STRENGTH)
        public double attackKnockback;
        public boolean attackAnimation;
        public AABB attackHitbox;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "octopusform");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            selectRange = abilityNode.getNode("select-range").getDouble(10.0);
            selectMaxDistance = abilityNode.getNode("select-max-distance").getDouble(30.0);
            sourceTravelSpeed = abilityNode.getNode("source-travel-speed").getDouble(1.0);
            chargeTime = abilityNode.getNode("charge-time").getLong(500);
            radius = abilityNode.getNode("radius").getDouble(3.0);

            attackDamage = abilityNode.getNode("attack").getNode("damage").getDouble(2.0);
            attackKnockback = abilityNode.getNode("attack").getNode("knockback").getDouble(1.75);
            attackAnimation = abilityNode.getNode("attack").getNode("animation").getBoolean(true);

            double x = abilityNode.getNode("attack").getNode("hitbox").getNode("x").getDouble(2.0);
            double y = abilityNode.getNode("attack").getNode("hitbox").getNode("y").getDouble(2.5);
            double z = abilityNode.getNode("attack").getNode("hitbox").getNode("z").getDouble(2.0);

            attackHitbox = new AABB(new Vector3D(-x / 2.0, 0.0, -z / 2.0), new Vector3D(x / 2.0, y, z / 2.0));
        }
    }
}
