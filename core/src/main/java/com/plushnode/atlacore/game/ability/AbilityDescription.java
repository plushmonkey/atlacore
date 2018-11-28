package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.User;

public interface AbilityDescription {
    String getName();

    String getDescription();
    AbilityDescription setDescription(String desc);

    String getInstructions();
    AbilityDescription setInstructions(String instructions);

    long getCooldown();
    AbilityDescription setCooldown(long milliseconds);

    boolean isEnabled();
    AbilityDescription setEnabled(boolean enabled);
    
    boolean isHidden();
    AbilityDescription setHidden(boolean hidden);

    boolean isHarmless();
    AbilityDescription setHarmless(boolean harmless);

    boolean isActivatedBy(ActivationMethod method);
    boolean isAbility(Ability ability);

    Ability createAbility();

    Element getElement();

    boolean canBypassCooldown();
    boolean canSourcePlant(User user);
}
