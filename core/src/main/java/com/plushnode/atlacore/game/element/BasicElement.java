package com.plushnode.atlacore.game.element;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.AbilityRegistry;
import com.plushnode.atlacore.util.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BasicElement implements Element {
    private String name;
    private ChatColor color;

    public BasicElement(String name, ChatColor color) {
        this.name = name;
        this.color = color;
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
    public List<AbilityDescription> getPassives() {
        return new ArrayList<>();
    }

    @Override
    public void addPassive(AbilityDescription passive) {

    }

    @Override
    public ChatColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return color + getName();
    }
}
