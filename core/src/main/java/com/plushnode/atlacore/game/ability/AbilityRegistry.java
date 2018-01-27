package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AbilityRegistry extends Configurable {
    private Map<String, AbilityDescription> abilities = new HashMap<>();

    @Override
    public void onConfigReload() {
        if (abilities == null) return;

        for (AbilityDescription abilityDesc : getAbilities()) {
            CommentedConfigurationNode elementNode = config.getNode("abilities",
                    abilityDesc.getElement().getName().toLowerCase());

            CommentedConfigurationNode node;
            if (abilityDesc.isActivatedBy(ActivationMethod.Sequence)) {
                node = elementNode.getNode("sequences", abilityDesc.getName().toLowerCase());
            } else {
                node = elementNode.getNode(abilityDesc.getName().toLowerCase());
            }

            long cooldownMS = node.getNode("cooldown").getLong();
            boolean enabled = node.getNode("enabled").getBoolean();

            abilityDesc.setCooldown(cooldownMS);
            abilityDesc.setEnabled(enabled);

            Game.info(abilityDesc.getName() + " cooldown set to " + cooldownMS);
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
