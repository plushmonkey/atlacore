package com.plushnode.atlacore.game.slots;

import com.plushnode.atlacore.game.ability.AbilityDescription;

public interface AbilitySlotContainer {
    AbilityDescription getAbility(int slot);
    void setAbility(int slot, AbilityDescription desc);
}
