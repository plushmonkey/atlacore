package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
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
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class WaterArmsPull implements Ability {
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

                arm.setState(new PullArmState(arm, userConfig));
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

    public static class PullArmState extends Arm.ExtensionArmState {
        private Config userConfig;

        PullArmState(Arm arm, Config userConfig) {
            super(arm, userConfig.speed, userConfig.length);

            this.userConfig = userConfig;
        }

        @Override
        public void act(Location location) {
            Sphere collider = new Sphere(location, userConfig.entityCollisionRadius);

            CollisionUtil.handleEntityCollisions(arm.getUser(), collider, entity -> {
                Vector3D toArm = VectorUtil.normalizeOrElse(arm.getEnd().subtract(entity.getLocation()).toVector(), Vector3D.PLUS_I);

                entity.setVelocity(toArm.scalarMultiply(userConfig.strength));

                return false;
            }, true);
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
