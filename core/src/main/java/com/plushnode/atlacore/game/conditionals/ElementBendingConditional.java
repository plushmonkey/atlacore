package com.plushnode.atlacore.game.conditionals;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;

// Checks if the user has the required element to activate the ability.
public class ElementBendingConditional implements BendingConditional {
    @Override
    public boolean canBend(User user, AbilityDescription desc) {
        return desc != null && user.hasElement(desc.getElement());
    }
}
