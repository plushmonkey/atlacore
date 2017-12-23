package com.plushnode.atlacore.ability;

import com.plushnode.atlacore.element.Element;

import java.util.List;

public class GenericAbilityDescription<T extends Ability> implements AbilityDescription {
    private String name;
    private String description;
    private long cooldown;
    private Element element;
    private List<ActivationMethod> activationMethods;
    private final Class<T> type;
    private boolean enabled;
    private boolean harmless;

    public GenericAbilityDescription(String name, String desc, Element element, int cooldown, List<ActivationMethod> activation, Class<T> type, boolean harmless) {
        super();
        this.name = name;
        this.description = desc;
        this.cooldown = cooldown;
        this.element = element;
        this.activationMethods = activation;
        this.type = type;
        this.harmless = harmless;
        this.enabled = true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
    public void setCooldown(long milliseconds) {
        this.cooldown = milliseconds;
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

    public boolean isAbility(Ability ability) {
        if (ability == null) return false;
        return this.type.isAssignableFrom(ability.getClass());
    }
}
