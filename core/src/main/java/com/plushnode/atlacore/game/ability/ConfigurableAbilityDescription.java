package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.element.Element;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Arrays;
import java.util.List;

public class ConfigurableAbilityDescription<T extends Ability> extends GenericAbilityDescription<T> {
    private List<String> nodePath;

    public ConfigurableAbilityDescription(String name, Element element, int cooldown, Class<T> type, ActivationMethod... activations) {
        super(name, element, cooldown, type, activations);
    }

    public void setConfigNode(String... nodePath) {
        this.nodePath = Arrays.asList(nodePath);
    }

    @Override
    public CommentedConfigurationNode getConfigNode() {
        CommentedConfigurationNode config = Game.plugin.getCoreConfig();

        return config.getNode(nodePath.toArray());
    }
}
