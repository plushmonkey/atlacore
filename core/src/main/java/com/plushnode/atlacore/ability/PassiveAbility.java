package com.plushnode.atlacore.ability;

import com.plushnode.atlacore.User;

public abstract class PassiveAbility implements Ability {
    public boolean onAbilityChange(User user, AbilityDescription oldAbility, AbilityDescription newAbility) {
        return false;
    }
}
