package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.game.conditionals.CompositeBendingConditional;
import com.plushnode.atlacore.game.element.Element;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
    void setCooldown(AbilityDescription abilityDesc, long duration);
    boolean isOnCooldown(AbilityDescription abilityDesc);

    default void setCooldown(Ability ability) {
        if (ability != null) {
            setCooldown(ability.getDescription());
        }
    }

    default void setCooldown(Ability ability, long duration) {
        if (ability != null) {
            setCooldown(ability.getDescription(), duration);
        }
    }

    Map<AbilityDescription, Long> getCooldowns();

    default boolean isSneaking() {
        // Non-players are always considered sneaking so they can charge abilities.
        // Scripted entities can call a method on the ability to manually launch things.
        return true;
    }

    default void setSneaking(boolean sneaking) {

    }

    default boolean isSprinting() {
        return true;
    }

    default void setSprinting(boolean sprinting) {

    }

    default boolean getAllowFlight() {
        return true;
    }

    default boolean isFlying() {
        return false;
    }

    default void setAllowFlight(boolean allow) {

    }

    default void setFlying(boolean flying) {

    }

    default void validateSlots() {
        for (int i = 1; i <= 9; ++i) {
            AbilityDescription desc = getSlotAbility(i);
            if (desc == null) continue;

            if (!hasElement(desc.getElement())) {
                setSlotAbility(i, null);
            }
        }
    }

    boolean hasPermission(String permission);

    boolean canBend(AbilityDescription abilityDescription);
    CompositeBendingConditional getBendingConditional();
    void setBendingConditional(CompositeBendingConditional cond);
}
