package com.plushnode.atlacore.game.conditionals;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;

// Checks if the ability is enabled.
public class EnabledBendingConditional implements BendingConditional {
    @Override
    public boolean canBend(User user, AbilityDescription desc) {
        return desc != null && desc.isEnabled();
    }
}
