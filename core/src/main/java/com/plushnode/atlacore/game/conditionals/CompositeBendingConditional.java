package com.plushnode.atlacore.game.conditionals;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeBendingConditional implements BendingConditional {
    private List<BendingConditional> conditionals = new ArrayList<>();

    @Override
    public boolean canBend(User user, AbilityDescription desc) {
        return !conditionals.stream().anyMatch((cond) -> !cond.canBend(user, desc));
    }

    public void add(BendingConditional... conds) {
        conditionals.addAll(Arrays.asList(conds));
    }

    public void add(Collection<BendingConditional> conds) {
        conditionals.addAll(conds);
    }

    public void add(BendingConditional conditional) {
        conditionals.add(conditional);
    }

    public void remove(BendingConditional conditional) {
        conditionals.remove(conditional);
    }

    public boolean hasType(Class<? extends BendingConditional> type) {
        return conditionals.stream().anyMatch(cond -> type.isAssignableFrom(cond.getClass()));
    }

    // Returns all of the conditionals that were removed.
    public List<BendingConditional> removeType(Class<? extends BendingConditional> type) {
        // Filter out any conditionals that match the provided type.
        List<BendingConditional> filtered = conditionals.stream()
                .filter((cond) -> type.isAssignableFrom(cond.getClass()))
                .collect(Collectors.toList());
        conditionals.removeAll(filtered);
        return filtered;
    }
}
