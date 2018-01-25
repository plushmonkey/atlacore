package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.platform.User;

public abstract class PassiveAbility implements Ability {
    public boolean onAbilityChange(User user, AbilityDescription oldAbility, AbilityDescription newAbility) {
        return false;
    }
}
