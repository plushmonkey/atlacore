package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.block.TempBlock;
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
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class WaterArmsSpear implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Location location;
    private double distance;
    private double length;
    // Stores a list of blocks that were added for each update.
    private Deque<List<TempBlock>> tempBlocks = new ArrayDeque<>();
    private DirectionStrategy directionStrategy = new UserDirectionStrategy();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        WaterArms instance = WaterArms.getInstance(user);

        if (instance != null) {
            Arm arm = instance.getAndToggleArm();

            if (arm != null && arm.isFull()) {
                this.user = user;
                this.location = arm.getEnd();
                this.length = 1;
                this.distance = 0.0;

                recalculateConfig();

                // Destroy the current arm
                arm.clear();
                arm.setState(null);

                return true;
            }
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        // Slowly dissipate the spear if it goes out of range.
        if (this.distance > userConfig.range) {
            if (!this.tempBlocks.isEmpty()) {
                List<TempBlock> tbs = this.tempBlocks.pop();

                tbs.forEach(TempBlock::reset);
            }

            return this.tempBlocks.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
        }

        Vector3D direction = directionStrategy.getDirection();

        this.length = Math.min(this.length + userConfig.speed, userConfig.length);

        Location previous = this.location;
        this.location = this.location.add(direction.scalarMultiply(userConfig.speed));
        this.distance += userConfig.speed;

        if (this.distance > userConfig.range) {
            return UpdateResult.Continue;
        }

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        boolean hit = CollisionUtil.handleEntityCollisions(user, new Sphere(this.location, userConfig.entityCollisionRadius), entity -> {
            if (Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                ((LivingEntity) entity).damage(userConfig.damage);
                freezeEntity((LivingEntity) entity);
            }
            return true;
        }, true);

        if (hit) {
            return UpdateResult.Remove;
        }

        List<Block> blocks = new ArrayList<>();

        for (Block block : RayCaster.blockArray(user.getWorld(), new Ray(previous, direction), userConfig.speed)) {
            if (!MaterialUtil.isTransparent(block) && block.getType() != Material.WATER) {
                freeze();
                return UpdateResult.Remove;
            }

            blocks.add(block);
        }

        pushTempBlocks(blocks);

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        clear();
    }

    private void freezeEntity(LivingEntity entity) {
        freeze();

        Block eyeBlock = entity.getEyeLocation().getBlock();

        for (Block block : WorldUtil.getNearbyBlocks(entity.getLocation(), userConfig.radius)) {
            if (block.equals(eyeBlock)) continue;

            if (MaterialUtil.isTransparent(block) || block.getType() == Material.WATER) {
                new TempBlock(block, Material.ICE, userConfig.duration);
            }
        }
    }

    private void freeze() {
        if (userConfig.duration <= 0) return;

        List<Block> blocks = new ArrayList<>();

        for (List<TempBlock> tbs : tempBlocks) {
            for (TempBlock tb : tbs) {
                blocks.add(tb.getPreviousState().getBlock());
            }
        }

        clear();

        for (Block block : blocks) {
            new TempBlock(block, Material.ICE, userConfig.duration);
        }
    }

    private void pushTempBlocks(List<Block> blocks) {
        // Pop off enough TempBlock lists to make room for a new one.
        int maxLength = (int)(userConfig.length / userConfig.speed);

        while (tempBlocks.size() >= maxLength) {
            List<TempBlock> tbs = this.tempBlocks.pop();

            tbs.forEach(TempBlock::reset);
        }

        List<TempBlock> tbs = new ArrayList<>();

        for (Block block : blocks) {
            tbs.add(new TempBlock(block, Material.WATER));
        }

        tempBlocks.add(tbs);
    }

    private void clear() {
        for (List<TempBlock> tbs : tempBlocks) {
            tbs.forEach(TempBlock::reset);
        }
        tempBlocks.clear();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterArmsSpear";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        userConfig = Game.getAttributeSystem().calculate(this, config);

        if (userConfig.fixedDirection) {
            Location target = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.range));
            Vector3D direction = target.subtract(this.location).toVector().normalize();

            this.directionStrategy = new FixedDirectionStrategy(direction);
        } else {
            this.directionStrategy = new UserDirectionStrategy();
        }
    }

    private interface DirectionStrategy {
        Vector3D getDirection();
    }

    private class FixedDirectionStrategy implements DirectionStrategy {
        private Vector3D direction;

        FixedDirectionStrategy(Vector3D direction) {
            this.direction = direction;
        }

        public Vector3D getDirection() {
            return this.direction;
        }
    }

    private class UserDirectionStrategy implements DirectionStrategy {
        public Vector3D getDirection() {
            Location target = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.range));
            return target.subtract(location).toVector().normalize();
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.AMOUNT)
        public int length;
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.RADIUS)
        public double radius;
        public boolean fixedDirection;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms", "spear");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            speed = abilityNode.getNode("speed").getDouble(2.0);
            range = abilityNode.getNode("range").getDouble(30.0);
            length = abilityNode.getNode("length").getInt(18);
            duration = abilityNode.getNode("duration").getLong(4500);
            radius = abilityNode.getNode("radius").getDouble(2.0);
            fixedDirection = abilityNode.getNode("fixed-direction").getBoolean(false);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
        }
    }
}
