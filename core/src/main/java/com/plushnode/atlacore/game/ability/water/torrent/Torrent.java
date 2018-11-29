package com.plushnode.atlacore.game.ability.water.torrent;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class Torrent implements Ability {
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
        return "Torrent";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    interface State {
        boolean update();
        void onPunch();
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "torrent");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
        }
    }
}
