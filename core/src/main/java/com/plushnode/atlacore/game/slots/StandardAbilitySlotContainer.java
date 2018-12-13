package com.plushnode.atlacore.game.slots;

import com.plushnode.atlacore.game.ability.AbilityDescription;

public class StandardAbilitySlotContainer implements AbilitySlotContainer {
    private AbilityDescription[] boundAbilities = new AbilityDescription[9];

    @Override
    public AbilityDescription getAbility(int slot) {
        return boundAbilities[slot - 1];
    }

    @Override
    public void setAbility(int slot, AbilityDescription desc) {
        boundAbilities[slot - 1] = desc;
    }
}
