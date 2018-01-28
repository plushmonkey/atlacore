package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.game.ability.Ability;

public class Collision {
    private Ability firstAbility;
    private Ability secondAbility;
    private boolean removeFirst;
    private boolean removeSecond;
    private Collider firstCollider;
    private Collider secondCollider;

    public Collision(Ability first, Ability second,
                     boolean removeFirst, boolean removeSecond,
                     Collider firstCollider, Collider secondCollider)
    {
        this.firstAbility = first;
        this.secondAbility = second;
        this.removeFirst = removeFirst;
        this.removeSecond = removeSecond;
        this.firstCollider = firstCollider;
        this.secondCollider = secondCollider;
    }

    public Ability getFirstAbility() {
        return firstAbility;
    }

    public Ability getSecondAbility() {
        return secondAbility;
    }

    public boolean shouldRemoveFirst() {
        return removeFirst;
    }

    public boolean shouldRemoveSecond() {
        return removeSecond;
    }

    public Collider getFirstCollider() {
        return firstCollider;
    }

    public Collider getSecondCollider() {
        return secondCollider;
    }
}
