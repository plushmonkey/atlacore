package com.plushnode.atlacore.game.element;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.AbilityRegistry;

import java.util.ArrayList;
import java.util.List;

public class BasicElement implements Element {
    private AbilityRegistry abilityRegistry = new AbilityRegistry();
    private String name;

    public BasicElement(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    @Override
    public List<AbilityDescription> getPassives() {
        return new ArrayList<>();
    }

    @Override
    public void addPassive(AbilityDescription passive) {

    }
}
