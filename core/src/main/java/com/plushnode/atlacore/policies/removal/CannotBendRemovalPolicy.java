package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.game.conditionals.CompositeBendingConditional;
import com.plushnode.atlacore.game.conditionals.CooldownBendingConditional;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;

import java.util.ArrayList;
import java.util.List;

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
        CompositeBendingConditional cond = user.getBendingConditional();

        List<BendingConditional> removed = new ArrayList<>();

        if (ignoreCooldowns) {
            // Remove the cooldown check and add it again after.
            removed = cond.removeType(CooldownBendingConditional.class);
        }

        boolean result = user.canBend(abilityDesc);

        cond.add(removed);

        return !result;
    }

    @Override
    public String getName() {
        return "CannotBend";
    }
}
