package com.plushnode.atlacore.game.ability.fire.sequences;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.FireTick;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class JetBlaze implements Ability {
    public static Config config = new Config();

    private FireJet jet;
    private Config userConfig;
    private List<Entity> affected = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (user.isOnCooldown(Game.getAbilityRegistry().getAbilityByName("FireJet"))) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        jet = new FireJet();

        if (!jet.activate(user, ActivationMethod.Punch)) {
            return false;
        }

        this.userConfig = Game.getAttributeSystem().calculate(this, config);

        jet.setDuration(userConfig.duration);
        jet.setSpeed(userConfig.speed);

        user.setCooldown(jet, userConfig.cooldown);
        user.setCooldown(this, userConfig.cooldown);

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        if (!Game.getProtectionSystem().canBuild(getUser(), getUser().getLocation())) {
            return UpdateResult.Remove;
        }

        Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE,
                0.6f, 0.6f, 0.6f, 0.0f, 20,
                getUser().getLocation());

        Sphere collider = new Sphere(getUser().getLocation().toVector(), userConfig.entityCollisionRadius);
        
        CollisionUtil.handleEntityCollisions(getUser(), collider, (entity) -> {
            if (entity instanceof LivingEntity && !affected.contains(entity)) {
                affected.add(entity);
                ((LivingEntity) entity).damage(userConfig.damage, getUser());
                FireTick.set(entity, userConfig.fireTicks);
            }

            return false;
        }, true);

        return jet.update();
    }

    @Override
    public void destroy() {
        jet.destroy();
    }

    @Override
    public User getUser() {
        return jet.getUser();
    }

    @Override
    public String getName() {
        return "JetBlaze";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.DURATION)
        public int fireTicks;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "sequences", "jetblaze");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(7000);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            duration = abilityNode.getNode("duration").getLong(4000);
            damage = abilityNode.getNode("damage").getDouble(2.0);
            fireTicks = abilityNode.getNode("fire-ticks").getInt(40);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(3.0);
        }
    }
}
