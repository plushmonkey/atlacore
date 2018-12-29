package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.Levelled;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class WaterArmsGrab implements Ability {
    public static Config config = new Config();

    private User user;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        WaterArms instance = WaterArms.getInstance(user);

        if (instance != null) {
            Arm arm = instance.getAndToggleArm();

            if (arm != null && arm.isFull()) {
                this.user = arm.getUser();

                Config userConfig = Game.getAttributeSystem().calculate(this, config);

                GrabData grabData = instance.getInstanceData(GrabData.class);
                if (grabData == null) {
                    instance.putInstanceData(GrabData.class, new GrabData());
                }

                arm.setState(new GrabExtensionArmState(arm, userConfig, instance));
            }
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        return UpdateResult.Remove;
    }

    @Override
    public void destroy() {

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterArmsGrab";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    public static class GrabExtensionArmState extends Arm.ExtensionArmState {
        private Config userConfig;
        private Entity target;
        private WaterArms instance;
        private boolean acted;

        GrabExtensionArmState(Arm arm, Config userConfig, WaterArms instance) {
            super(arm, userConfig.speed, userConfig.length);

            this.userConfig = userConfig;
            this.instance = instance;
            this.acted = false;
        }

        @Override
        public void act(Location location) {
            if (acted) return;

            GrabData grabData = instance.getInstanceData(GrabData.class);

            if (target == null) {
                int lookup = (int)Math.ceil(userConfig.radius + 1);
                for (Entity entity : arm.getUser().getWorld().getNearbyEntities(location, lookup, lookup, lookup)) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (grabData.entities.contains(entity)) continue;

                    double distSq = entity.getLocation().distanceSquared(location);

                    if (distSq <= userConfig.radius * userConfig.radius) {
                        target = entity;
                        grabData.entities.add(target);
                        break;
                    }
                }
            }

            if (speed < 0.0 && target != null) {
                this.acted = true;
                arm.setState(new HoldArmState(arm, userConfig, target, this));
            }
        }
    }

    public static class HoldArmState implements Arm.ArmState {
        private Arm arm;
        private Config userConfig;
        private Entity entity;
        private List<TempBlock> tempBlocks = new ArrayList<>();
        private long startTime;
        private GrabExtensionArmState previousState;

        HoldArmState(Arm arm, Config userConfig, Entity target, GrabExtensionArmState previousState) {
            this.arm = arm;
            this.userConfig = userConfig;
            this.entity = target;
            this.startTime = System.currentTimeMillis();
            this.previousState = previousState;
        }

        @Override
        public boolean update() {
            clear();

            if (System.currentTimeMillis() > startTime + userConfig.duration) {
                arm.setState(previousState);
                return true;
            }

            if (!entity.getWorld().equals(arm.getUser().getWorld()) || !entity.isValid()) {
                arm.setState(previousState);
                return true;
            }


            Vector3D direction = getDirection();
            Location end = arm.getEnd();
            int extendedLength = userConfig.length;

            for (int i = 0; i < userConfig.length; ++i) {
                Location current = end.add(direction.scalarMultiply(i));
                Block block = current.getBlock();
                if (block.getType() == Material.WATER) continue;

                if (!MaterialUtil.isTransparent(block)) {
                    // Set the extended length to the current length so the entity is pulled to the last rendered block.
                    extendedLength = i;
                    break;
                }

                int level = (int)((i / Math.ceil(userConfig.length)) * 3);

                tempBlocks.add(new TempBlock(block, Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(level))));
            }

            Location extensionEnd = getExtensionEnd(extendedLength);
            Vector3D toEnd = extensionEnd.subtract(entity.getLocation()).toVector();

            if (toEnd.getNormSq() > 0.0) {
                entity.setVelocity(toEnd.normalize().scalarMultiply(0.65));
                entity.setFallDistance(0.0f);
            }

            return true;
        }

        private Vector3D getDirection() {
            Location begin = arm.getEnd().subtract(arm.getUser().getDirection()).getBlock().getLocation().add(0.5, 0.5, 0.5);
            Location target = arm.getUser().getEyeLocation().add(arm.getUser().getDirection().scalarMultiply(arm.getLength() + userConfig.length - 1));

            target = target.getBlock().getLocation().add(0.5, 0.5, 0.5);

            return target.subtract(begin).toVector().normalize();
        }

        private Location getExtensionEnd(int length) {
            Location begin = arm.getEnd().subtract(arm.getUser().getDirection()).getBlock().getLocation().add(0.5, 0.5, 0.5);
            Location target = arm.getUser().getEyeLocation().add(arm.getUser().getDirection().scalarMultiply(arm.getLength() + userConfig.length - 1));

            target = target.getBlock().getLocation().add(0.5, 0.5, 0.5);

            Vector3D direction = target.subtract(begin).toVector().normalize();

            return begin.add(direction.scalarMultiply(length)).getBlock().getLocation().add(0.5, 0.5, 0.5);
        }

        @Override
        public void clear() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();
        }

        @Override
        public boolean canActivate() {
            return true;
        }
    }

    private static class GrabData {
        List<Entity> entities = new ArrayList<>();
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public int length;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.DURATION)
        public long duration;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms", "grab");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            length = abilityNode.getNode("length").getInt(6);
            radius = abilityNode.getNode("radius").getDouble(2.0);
            duration = abilityNode.getNode("duration").getLong(3500);
        }
    }
}
