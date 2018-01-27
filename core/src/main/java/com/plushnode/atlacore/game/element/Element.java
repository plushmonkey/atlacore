package com.plushnode.atlacore.game.element;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.AbilityRegistry;
import com.plushnode.atlacore.util.ChatColor;

import java.util.List;

public interface Element {
    String getName();
    String getPermission();

    ChatColor getColor();

    List<AbilityDescription> getPassives();
    void addPassive(AbilityDescription passive);
}
