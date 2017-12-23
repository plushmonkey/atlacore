package com.plushnode.atlacore.ability;

public interface AbilityDescription {
    String getName();
    String getDescription();

    long getCooldown();
    void setCooldown(long milliseconds);

    boolean isEnabled();
    void setEnabled(boolean enabled);

    boolean isActivatedBy(ActivationMethod method);
    boolean isAbility(Ability ability);

    Ability createAbility();
}
