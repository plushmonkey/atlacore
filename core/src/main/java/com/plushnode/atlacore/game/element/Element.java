package com.plushnode.atlacore.game.element;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.AbilityRegistry;

import java.util.List;

public interface Element {
    String getName();
    String getPermission();

    String toString();

    AbilityRegistry getAbilityRegistry();

    List<AbilityDescription> getPassives();
    void addPassive(AbilityDescription passive);
}
