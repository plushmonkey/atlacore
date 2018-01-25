package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.element.Element;

import java.util.List;

public interface User extends LivingEntity {
    void addElement(Element element);
    void removeElement(Element element);
    List<Element> getElements();
    boolean hasElement(Element element);

    boolean canBind(AbilityDescription abilityDesc);
    void setSlotAbility(int slot, AbilityDescription abilityDesc);
    AbilityDescription getSlotAbility(int slot);
    AbilityDescription getSelectedAbility();
    void clearSlots();

    void setCooldown(AbilityDescription abilityDesc);
    boolean isOnCooldown(AbilityDescription abilityDesc);

    default void setCooldown(Ability ability) {
        if (ability != null) {
            setCooldown(ability.getDescription());
        }
    }
}
