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
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.Levelled;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class WaterArmsPull implements Ability {
    public static Config config = new Config();

    private User user;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        WaterArms instance = WaterArms.getInstance(user);

        if (instance != null) {
            Arm arm = instance.getAndToggleArm();

            if (arm != null) {
                arm.setState(new PullArmState(arm));
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
        return "WaterArmsPull";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    public static class PullArmState implements Arm.ArmState {
        private Arm arm;
        private int extension;
        private double speed;
        private List<TempBlock> tempBlocks = new ArrayList<>();
        private Config userConfig;

        PullArmState(Arm arm) {
            this.arm = arm;
            this.extension = 0;

            WaterArmsPull pullAbility = new WaterArmsPull();
            pullAbility.user = arm.getUser();

            this.userConfig = Game.getAttributeSystem().calculate(pullAbility, config);
            this.speed = userConfig.speed;
        }

        @Override
        public boolean update() {
            Location begin = arm.getEnd().subtract(arm.getUser().getDirection()).getBlock().getLocation().add(0.5, 0.5, 0.5);

            Location target = arm.getUser().getEyeLocation().add(arm.getUser().getDirection().scalarMultiply(arm.getLength() + userConfig.length + 1));
            target = target.getBlock().getLocation().add(0.5, 0.5, 0.5);
            Vector3D direction = target.subtract(begin).toVector().normalize();

            this.extension += speed;

            clear();

            if (this.extension >= userConfig.length) {
                this.extension = userConfig.length;
                speed = -userConfig.speed;
            }

            for (Block block : RayCaster.blockArray(arm.getUser().getWorld(), new Ray(begin, direction), extension + 1)) {
                if (block.getType() == Material.WATER) continue;
                if (!MaterialUtil.isTransparent(block)) {
                    speed = -userConfig.speed;
                    break;
                }

                Sphere collider = new Sphere(block.getLocation().add(0.5, 0.5, 0.5), userConfig.entityCollisionRadius);

                CollisionUtil.handleEntityCollisions(arm.getUser(), collider, entity -> {
                    Vector3D toArm = VectorUtil.normalizeOrElse(arm.getEnd().subtract(entity.getLocation()).toVector(), Vector3D.PLUS_I);

                    entity.setVelocity(toArm.scalarMultiply(userConfig.strength));

                    return false;
                }, true);

                tempBlocks.add(new TempBlock(block, Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(3))));
            }

            // Set it back to default state once the arm retracts.
            if (this.extension <= 0) {
                clear();
                arm.setState(new Arm.DefaultArmState());
            }

            return true;
        }

        @Override
        public void clear() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.STRENGTH)
        public double strength;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public int length;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms", "pull");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            strength = abilityNode.getNode("strength").getDouble(0.15);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            length = abilityNode.getNode("length").getInt(3);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
        }
    }
}
