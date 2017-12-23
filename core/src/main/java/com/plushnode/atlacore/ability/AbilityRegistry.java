package com.plushnode.atlacore.ability;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AbilityRegistry {
    private Map<String, AbilityDescription> abilities;

    public AbilityRegistry() {
        abilities = new HashMap<>();
    }

    public boolean registerAbility(AbilityDescription abilityDesc) {
        abilities.put(abilityDesc.getName().toLowerCase(), abilityDesc);
        return true;
    }

    public boolean unregisterAbility(String name) {
        return abilities.remove(name.toLowerCase()) != null;
    }

    public AbilityDescription getAbilityByName(String name) {
        return abilities.get(name.toLowerCase());
    }

    public boolean hasAbility(AbilityDescription abilityDesc) {
        return abilities.values().contains(abilityDesc);
    }

    public Collection<AbilityDescription> getAbilities() {
        return abilities.values();
    }

    public AbilityDescription getAbilityDescription(Ability ability) {
        return abilities.get(ability.getName().toLowerCase());
    }
}
