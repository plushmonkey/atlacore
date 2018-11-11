package com.plushnode.atlacore.game.attribute;

import com.plushnode.atlacore.game.ability.Ability;

public interface ModifyPolicy {
    boolean shouldModify(Ability ability);
}
