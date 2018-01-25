package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public interface Ability {
    // return true if the ability was activated
    boolean activate(User user, ActivationMethod method);
    // Return true to destroy instance
    boolean update();

    void destroy();

    // Called when player changes hot bar slot. Default is to do destroy the instance if the new slot is different.
    // Return true to have the instance destroyed automatically.
    default boolean onAbilityChange(User user, AbilityDescription oldAbility, AbilityDescription newAbility) {
        return newAbility == null || !newAbility.isAbility(this);
    }

    default void onConfigReload(CommentedConfigurationNode config) {

    }

    String getName();
    default AbilityDescription getDescription() {
        return Game.getAbilityRegistry().getAbilityDescription(this);
    }
}
