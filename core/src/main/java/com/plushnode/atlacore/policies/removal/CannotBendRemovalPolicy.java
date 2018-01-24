package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.entity.user.Player;
import com.plushnode.atlacore.entity.user.User;
import com.plushnode.atlacore.ability.AbilityDescription;

public class CannotBendRemovalPolicy implements RemovalPolicy {
    private User user;
    private boolean ignoreBinds;
    private boolean ignoreCooldowns;
    private AbilityDescription abilityDesc;

    public CannotBendRemovalPolicy(User user, AbilityDescription abilityDesc) {
        this(user, abilityDesc, false, false);
    }

    public CannotBendRemovalPolicy(User user, AbilityDescription abilityDesc, boolean ignoreBinds, boolean ignoreCooldowns) {
        this.user = user;
        this.abilityDesc = abilityDesc;
        this.ignoreBinds = ignoreBinds;
        this.ignoreCooldowns = ignoreCooldowns;
    }

    @Override
    public boolean shouldRemove() {
        if (!(user instanceof Player)) return false;

        // todo: implementation

        return false;
    }

    @Override
    public String getName() {
        return "CannotBend";
    }
}
