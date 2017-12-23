package com.plushnode.atlacore.ability;

import java.util.List;

public class GenericAbilityDescription<T extends Ability> implements AbilityDescription {
    private String name;
    private String description;
    private long cooldown;
    private List<ActivationMethod> activationMethods;
    private final Class<T> type;
    private boolean enabled;

    public GenericAbilityDescription(String name, String desc, int cooldown, List<ActivationMethod> activation, Class<T> type) {
        super();
        this.name = name;
        this.description = desc;
        this.cooldown = cooldown;
        this.type = type;
        this.activationMethods = activation;
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

    public boolean isAbility(Ability ability) {
        if (ability == null) return false;
        return this.type.isAssignableFrom(ability.getClass());
    }
}
