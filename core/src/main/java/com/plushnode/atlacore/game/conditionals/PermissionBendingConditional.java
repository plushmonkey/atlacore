package com.plushnode.atlacore.game.conditionals;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;

// Checks if the user has the required permissions to activate the ability.
public class PermissionBendingConditional implements BendingConditional {
    @Override
    public boolean canBend(User user, AbilityDescription desc) {
        if (desc == null) return false;

        String permission = "atla.ability." + desc.getName();

        return user.hasPermission(permission);
    }
}
