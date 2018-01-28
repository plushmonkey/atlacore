package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.game.ability.AbilityDescription;

public class RegisteredCollision {
    private AbilityDescription first;
    private AbilityDescription second;
    private boolean removeFirst;
    private boolean removeSecond;

    public RegisteredCollision(AbilityDescription first, AbilityDescription second, boolean removeFirst, boolean removeSecond) {
        this.first = first;
        this.second = second;
        this.removeFirst = removeFirst;
        this.removeSecond = removeSecond;
    }

    public AbilityDescription getFirst() {
        return first;
    }

    public AbilityDescription getSecond() {
        return second;
    }

    public boolean shouldRemoveFirst() {
        return removeFirst;
    }

    public boolean shouldRemoveSecond() {
        return removeSecond;
    }
}
