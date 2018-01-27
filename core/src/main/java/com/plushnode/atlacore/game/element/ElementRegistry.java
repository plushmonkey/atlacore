package com.plushnode.atlacore.game.element;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.AbilityRegistry;
import com.plushnode.atlacore.config.Configurable;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.*;

public class ElementRegistry extends Configurable {
    private Set<Element> elements = new HashSet<>();

    public ElementRegistry() {
        super();
    }

    @Override
    public void onConfigReload() {
        if (elements == null) return;

        for (Element element : elements) {
            AbilityRegistry registry = element.getAbilityRegistry();

            if (registry == null || registry.getAbilities() == null) {
                continue;
            }

            for (AbilityDescription abilityDesc : registry.getAbilities()) {
                CommentedConfigurationNode node = config.getNode("abilities",
                        element.getName(), abilityDesc.getName().toLowerCase());

                long cooldownMS = node.getNode("cooldown").getLong();
                boolean enabled = node.getNode("enabled").getBoolean();

                abilityDesc.setCooldown(cooldownMS);
                abilityDesc.setEnabled(enabled);
            }
        }
    }

    public boolean registerElement(Element element) {
        this.elements.add(element);
        return true;
    }

    public Element getElementByName(String name) {
        for (Element element : elements) {
            if (element.getName().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }

    public AbilityDescription getAbilityByName(String name) {
        for (Element element : elements) {
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

    public List<Element> getElements() {
        return new ArrayList<>(elements);
    }
}

