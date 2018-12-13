package com.plushnode.atlacore.game.slots;

import com.plushnode.atlacore.game.ability.AbilityDescription;

import java.util.List;

public class MultiAbilitySlotContainer implements AbilitySlotContainer {
    private List<AbilityDescription> abilities;

    public MultiAbilitySlotContainer(List<AbilityDescription> abilities) {
        this.abilities = abilities;
    }

    @Override
    public AbilityDescription getAbility(int slot) {
        if (slot < 1 || slot > abilities.size()) return null;

        return abilities.get(slot - 1);
    }

    @Override
    public void setAbility(int slot, AbilityDescription desc) {

    }
}
