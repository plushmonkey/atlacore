package com.plushnode.atlacore.game.conditionals;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;

public interface BendingConditional {
    boolean canBend(User user, AbilityDescription desc);
}
