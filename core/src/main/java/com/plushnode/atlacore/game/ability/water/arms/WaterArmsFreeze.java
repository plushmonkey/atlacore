package com.plushnode.atlacore.game.ability.water.arms;

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
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Material;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class WaterArmsFreeze implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Location location;
    private Vector3D direction;
    private Location origin;
    private TempBlock tempBlock;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (method != ActivationMethod.Punch) return false;

        WaterArms instance = WaterArms.getInstance(user);
        if (instance == null) return false;

        this.user = user;
        recalculateConfig();

        FreezeData freezeData = instance.getInstanceData(FreezeData.class);

        if (freezeData == null) {
            freezeData = instance.putInstanceData(FreezeData.class, new FreezeData());
        }

        if (++freezeData.count >= userConfig.count && userConfig.count > 0) {
            Game.getAbilityInstanceManager().destroyInstance(user, instance);
        }

        Arm arm = instance.getAndToggleArm();

        this.location = arm.getEnd().add(user.getDirection()).getBlock().getLocation().add(0.5, 0.5, 0.5);

        Location target = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.range));
        this.direction = target.subtract(this.location).toVector().normalize();
        this.origin = this.location;
        this.tempBlock = null;

        return true;
    }

    @Override
    public UpdateResult update() {
        if (this.tempBlock != null) {
            this.tempBlock.reset();
        }

        Location previousLocation = this.location;
        this.location = this.location.add(this.direction.scalarMultiply(userConfig.speed));

        if (this.location.distanceSquared(this.origin) > userConfig.range * userConfig.range) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        boolean collision = CollisionUtil.handleBlockCollisions(new Sphere(previousLocation, userConfig.speed),
                previousLocation, this.location, true).getFirst();

        if (collision) {
            return UpdateResult.Remove;
        }

        this.tempBlock = new TempBlock(location.getBlock(), Material.ICE);
        Game.plugin.getParticleRenderer().display(ParticleEffect.SNOW_SHOVEL, (float)Math.random(), (float)Math.random(), (float)Math.random(), 0.05f, 5, this.location);

        AABB collider = AABB.BLOCK_BOUNDS.scale(userConfig.entityCollisionRadius * 2).at(this.location);
        boolean hit = CollisionUtil.handleEntityCollisions(user, collider, entity -> {
            ((LivingEntity)entity).damage(userConfig.damage);
            PotionEffect slowEffect = Game.plugin.getPotionFactory().createEffect(PotionEffectType.SLOWNESS, userConfig.slowTicks, userConfig.slowAmplifier, false, true);
            ((LivingEntity) entity).addPotionEffect(slowEffect);
            return true;
        }, true);

        return hit ? UpdateResult.Remove : UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        if (this.tempBlock != null) {
            tempBlock.reset();
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterArmsFreeze";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    private static class FreezeData {
        int count = 0;
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.AMOUNT)
        public int count;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.DURATION)
        public int slowTicks;
        @Attribute(Attributes.STRENGTH)
        public int slowAmplifier;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms", "freeze");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            count = abilityNode.getNode("count").getInt(8);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            range = abilityNode.getNode("range").getDouble(20.0);
            damage = abilityNode.getNode("damage").getDouble(2.0);
            slowTicks = abilityNode.getNode("slow-ticks").getInt(40);
            slowAmplifier = abilityNode.getNode("slow-amplifier").getInt(2);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
        }
    }
}
