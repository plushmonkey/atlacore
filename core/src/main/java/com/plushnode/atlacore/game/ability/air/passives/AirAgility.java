package com.plushnode.atlacore.game.ability.air.passives;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.PassiveAbility;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.PotionEffect;
import com.plushnode.atlacore.platform.PotionEffectType;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class AirAgility implements PassiveAbility {
    public static Config config = new Config();

    private User user;
    private Config userConfig;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        if (user.isDead() || !user.canBend(getDescription())) {
            return UpdateResult.Continue;
        }

        if (userConfig.jumpAmplifier > 0) {
            handlePotionEffect(PotionEffectType.JUMP_BOOST, userConfig.jumpAmplifier);
        }

        if (userConfig.speedAmplifier > 0) {
            handlePotionEffect(PotionEffectType.SPEED, userConfig.speedAmplifier);
        }

        return UpdateResult.Continue;
    }

    private void handlePotionEffect(PotionEffectType type, int amplifier) {
        PotionEffect effect = user.getPotionEffect(type);

        if (effect == null || effect.getDuration() < 20 || effect.getAmplifier() < amplifier) {
            effect = Game.plugin.getPotionFactory().createEffect(type, 100, amplifier - 1, true, false);
            user.addPotionEffect(effect, false);
        }
    }

    @Override
    public void destroy() {
        user.removePotionEffect(PotionEffectType.JUMP_BOOST);
        user.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirAgility";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.STRENGTH)
        public int speedAmplifier;
        @Attribute(Attributes.STRENGTH)
        public int jumpAmplifier;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "passives", "airagility");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            speedAmplifier = abilityNode.getNode("speed-amplifier").getInt(2);
            jumpAmplifier = abilityNode.getNode("jump-amplifier").getInt(2);
        }
    }
}
