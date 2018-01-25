package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.element.Element;

public interface AbilityDescription {
    String getName();
    String getDescription();

    long getCooldown();
    void setCooldown(long milliseconds);

    boolean isEnabled();
    void setEnabled(boolean enabled);
    
    boolean isHidden();
    void setHidden(boolean hidden);

    boolean isHarmless();

    boolean isActivatedBy(ActivationMethod method);
    boolean isAbility(Ability ability);

    Ability createAbility();

    Element getElement();
}
