package com.plushnode.atlacore.element;

import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.AbilityRegistry;
import com.plushnode.atlacore.config.Configurable;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.HashMap;
import java.util.Map;

public class ElementRegistry extends Configurable {
    private Map<Element, Element> elements = new HashMap<>();

    public ElementRegistry() {
        super();
    }

    @Override
    public void onConfigReload() {
        for (Element element : elements.values()) {
            AbilityRegistry registry = element.getAbilityRegistry();

            if (registry == null || registry.getAbilities() == null) {
                continue;
            }

            for (AbilityDescription abilityDesc : registry.getAbilities()) {
                String section = element.getName() + "." + abilityDesc.getName() + ".";
                CommentedConfigurationNode node = config.getNode("abilities",
                        element.getName(), abilityDesc.getName().toLowerCase());

                long cooldownMS = node.getNode("cooldown").getLong();
                boolean enabled = node.getNode("enabled").getBoolean();

                abilityDesc.setCooldown(cooldownMS);
                abilityDesc.setEnabled(enabled);

                // Create a fake ability to update static config for it
                Ability ability = abilityDesc.createAbility();
                ability.onConfigReload(config);
            }
        }
    }

    public boolean registerElement(Element element) {
        this.elements.put(element, element);
        return true;
    }

    public Element getElementByName(String name) {
        for (Element element : elements.values()) {
            if (element.getName().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }

    public AbilityDescription getAbilityByName(String name) {
        for (Element element : elements.values()) {
            AbilityRegistry registry = element.getAbilityRegistry();

            if (registry != null) {
                AbilityDescription abilityDesc = registry.getAbilityByName(name);
                if (abilityDesc != null) {
                    return abilityDesc;
                }
            }
        }
        return null;
    }
}

