package com.plushnode.atlacore.ability;

import com.plushnode.atlacore.Entity;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.config.Configuration;

public interface Ability {
    // return true if the ability can be created
    boolean create(Entity entity, ActivationMethod method);
    // Return true to destroy instance
    boolean update();

    void destroy();

    // Called when player changes hot bar slot. Default is to do destroy the instance if the new slot is different.
    // Return true to have the instance destroyed automatically.
    default boolean onAbilityChange(User user, AbilityDescription oldAbility, AbilityDescription newAbility) {
        return newAbility == null || !newAbility.isAbility(this);
    }

    default void onConfigReload(Configuration config) {

    }
}
