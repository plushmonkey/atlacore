package com.plushnode.atlacore.element;

import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.AbilityRegistry;

import java.util.List;

public interface Element {
    String getName();
    String getPermission();

    String toString();

    AbilityRegistry getAbilityRegistry();

    List<AbilityDescription> getPassives();
    void addPassive(AbilityDescription passive);
}
