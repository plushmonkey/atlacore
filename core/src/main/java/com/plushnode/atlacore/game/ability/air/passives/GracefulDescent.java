package com.plushnode.atlacore.game.ability.air.passives;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.PassiveAbility;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class GracefulDescent implements PassiveAbility {
    public static Config config = new Config();

    private User user;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        return true;
    }

    @Override
    public UpdateResult update() {
        return UpdateResult.Continue;
    }

    public static boolean isGraceful(User user) {
        if (Game.getAbilityInstanceManager().getPlayerInstances(user, GracefulDescent.class).isEmpty()) {
            return false;
        }

        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName("GracefulDescent");
        return user.canBend(desc);
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
        return "GracefulDescent";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Config extends Configurable {
        boolean enabled;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "passives", "gracefuldescent");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
        }
    }
}
