package com.plushnode.atlacore.game.ability.fire.sequences;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class JetBlast implements Ability {
    public static Config config = new Config();

    private FireJet jet;
    private Config userConfig;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (user.isOnCooldown(Game.getAbilityRegistry().getAbilityByName("FireJet"))) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, getDescription(), user.getLocation())) {
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
        return "JetBlast";
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

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "sequences", "jetblast");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            speed = abilityNode.getNode("speed").getDouble(1.2);
            duration = abilityNode.getNode("duration").getLong(5000);
        }
    }
}
