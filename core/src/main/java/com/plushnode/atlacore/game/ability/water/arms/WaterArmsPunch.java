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
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class WaterArmsPunch implements Ability {
    public static Config config = new Config();

    private User user;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        WaterArms instance = WaterArms.getInstance(user);

        if (instance != null) {
            Arm arm = instance.getAndToggleArm();

            if (arm != null && arm.isFull()) {
                this.user = user;

                Config userConfig = Game.getAttributeSystem().calculate(this, config);

                arm.setState(new PunchArmState(arm, userConfig));
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
        return "WaterArmsPunch";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    public static class PunchArmState extends Arm.ExtensionArmState {
        private Config userConfig;
        private boolean punched;

        PunchArmState(Arm arm, Config userConfig) {
            super(arm, userConfig.speed, userConfig.length);

            this.userConfig = userConfig;
            this.punched = false;
        }

        @Override
        public void act(Location location) {
            if (this.punched) return;

            Sphere collider = new Sphere(location, userConfig.entityCollisionRadius);

            CollisionUtil.handleEntityCollisions(arm.getUser(), collider, entity -> {
                Vector3D forward = VectorUtil.normalizeOrElse(entity.getLocation().subtract(arm.getEnd()).toVector(), Vector3D.PLUS_I);

                entity.setVelocity(forward.scalarMultiply(userConfig.knockback));
                ((LivingEntity)entity).damage(userConfig.damage);
                this.punched = true;

                return false;
            }, true);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.STRENGTH)
        public double knockback;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public int length;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms", "punch");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            knockback = abilityNode.getNode("knockback").getDouble(0.15);
            damage = abilityNode.getNode("damage").getDouble(1.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            length = abilityNode.getNode("length").getInt(6);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
        }
    }
}

