package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AbilityRegistry extends Configurable {
    private Map<String, AbilityDescription> abilities = new HashMap<>();

    @Override
    public void onConfigReload() {
        if (abilities == null) return;

        for (AbilityDescription abilityDesc : getAbilities()) {
            CommentedConfigurationNode node = abilityDesc.getConfigNode();

            long cooldownMS = node.getNode("cooldown").getLong();
            boolean enabled = node.getNode("enabled").getBoolean();
            String description = node.getNode("description").getString();
            String instructions = node.getNode("instructions").getString();

            abilityDesc.setCooldown(cooldownMS);
            abilityDesc.setEnabled(enabled);
            if (description != null) {
                abilityDesc.setDescription(description);
            }
            if (instructions != null) {
                abilityDesc.setInstructions(instructions);
            }
        }
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

    public void clear() {
        abilities.clear();
    }
}
