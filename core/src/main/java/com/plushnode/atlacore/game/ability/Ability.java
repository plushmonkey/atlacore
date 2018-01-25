package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public interface Ability {
    // return true if the ability was activated
    boolean activate(User user, ActivationMethod method);
    // Return UpdateResult.Remove if the ability should be removed.
    UpdateResult update();

    void destroy();

    String getName();
    default AbilityDescription getDescription() {
        return Game.getAbilityRegistry().getAbilityDescription(this);
    }
}
