package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.element.Element;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class MultiAbilityDescription<T extends Ability> extends GenericAbilityDescription<T> {
    private String parent;
    private String sub;
    private String displayName;

    public MultiAbilityDescription(String name, Element element, int cooldown, Class<T> type, ActivationMethod... activations) {
        super(name, element, cooldown, type, activations);
    }

    public void setConfigNode(String parent, String sub) {
        this.parent = parent.toLowerCase();
        this.sub = sub.toLowerCase();
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public CommentedConfigurationNode getConfigNode() {
        CommentedConfigurationNode config = Game.plugin.getCoreConfig();

        CommentedConfigurationNode elementNode = config.getNode("abilities", getElement().getName().toLowerCase());

        CommentedConfigurationNode node = elementNode;

        if (isActivatedBy(ActivationMethod.Sequence)) {
            node = elementNode.getNode("sequences");
        } else if (isActivatedBy(ActivationMethod.Passive)) {
            node = elementNode.getNode("passives");
        }

        return node.getNode(parent).getNode(sub);
    }
}
