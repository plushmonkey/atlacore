package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.ChatColor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Arrays;
import java.util.List;

public class GenericAbilityDescription<T extends Ability> implements AbilityDescription {
    private String name;
    private String description;
    private String instructions;
    private long cooldown;
    private Element element;
    private List<ActivationMethod> activationMethods;
    private final Class<T> type;
    private boolean enabled;
    private boolean harmless;
    private boolean hidden;
    private boolean cooldownBypass;
    private boolean sourcesPlants;

    public GenericAbilityDescription(String name, Element element, int cooldown, Class<T> type, ActivationMethod... activations) {
        super();
        this.name = name;
        this.description = "";
        this.instructions = "";
        this.cooldown = cooldown;
        this.element = element;
        this.activationMethods = Arrays.asList(activations);
        this.type = type;
        this.harmless = false;
        this.enabled = true;
        this.hidden = false;
        this.cooldownBypass = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public AbilityDescription setDescription(String desc) {
        this.description = desc;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getInstructions() {
        return instructions;
    }

    @Override
    public AbilityDescription setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public AbilityDescription setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public AbilityDescription setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public AbilityDescription setHarmless(boolean harmless) {
        this.harmless = harmless;
        return this;
    }

    @Override
    public boolean isHarmless() {
        return this.harmless;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public AbilityDescription setCooldown(long milliseconds) {
        this.cooldown = milliseconds;
        return this;
    }

    @Override
    public boolean isActivatedBy(ActivationMethod method) {
        return activationMethods.contains(method);
    }

    @Override
    public Ability createAbility() {
        try {
            return this.type.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Element getElement() {
        return this.element;
    }

    public void setCanBypassCooldown(boolean bypass) {
        this.cooldownBypass = bypass;
    }

    @Override
    public boolean canBypassCooldown() {
        return cooldownBypass;
    }

    public boolean isAbility(Ability ability) {
        if (ability == null) return false;
        return this.type.isAssignableFrom(ability.getClass());
    }

    @Override
    public String toString() {
        return element.getColor() + getName();
    }

    public void setSourcesPlants(boolean sources) {
        this.sourcesPlants = sources;
    }

    @Override
    public boolean canSourcePlant(User user) {
        return this.sourcesPlants && !user.isOnCooldown(this);
    }

    @Override
    public CommentedConfigurationNode getConfigNode() {
        CommentedConfigurationNode config = Game.plugin.getCoreConfig();

        CommentedConfigurationNode elementNode = config.getNode("abilities", getElement().getName().toLowerCase());

        CommentedConfigurationNode node;
        if (isActivatedBy(ActivationMethod.Sequence)) {
            node = elementNode.getNode("sequences", getName().toLowerCase());
        } else if (isActivatedBy(ActivationMethod.Passive)) {
            node = elementNode.getNode("passives", getName().toLowerCase());
        } else {
            node = elementNode.getNode(getName().toLowerCase());
        }

        return node;
    }
}
