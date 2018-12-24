package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.collision.Collision;
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
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class WaterArmsGrapple implements Ability {
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

                arm.setState(new GrappleArmState(arm, userConfig));
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
        return "WaterArmsGrapple";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    public static class GrappleArmState extends Arm.ExtensionArmState {
        private Config userConfig;
        private Block grappleTarget;

        GrappleArmState(Arm arm, Config userConfig) {
            super(arm, userConfig.speed, userConfig.length);

            this.userConfig = userConfig;
            this.grappleTarget = null;
        }

        @Override
        public void act(Location location) {
            if (this.speed < 0 && grappleTarget != null) {
                Location target = grappleTarget.getLocation().add(0.5, 0.5, 0.5);

                Vector3D toTarget = target.subtract(arm.getUser().getLocation()).toVector();

                if (toTarget.getNormSq() <= 0.0) return;

                arm.getUser().setVelocity(toTarget.normalize().scalarMultiply(userConfig.pullStrength));
                arm.getUser().setFallDistance(0.0f);
            }
        }

        @Override
        public void onBlockCollision(Block block) {
            grappleTarget = block;
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public int length;
        @Attribute(Attributes.STRENGTH)
        public double pullStrength;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms", "grapple");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            length = abilityNode.getNode("length").getInt(6);
            pullStrength = abilityNode.getNode("pull-strength").getDouble(1.25);
        }
    }
}
