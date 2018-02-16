package com.plushnode.atlacore.game.ability.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sequence {
    private static final int MAX_SEQUENCE_SIZE = 10;
    private List<AbilityAction> actions = new ArrayList<>();

    public Sequence() {

    }

    public Sequence(boolean initialize, AbilityAction... actions) {
        this.actions.addAll(Arrays.asList(actions));
    }

    public Sequence(AbilityAction action, AbilityAction... actions) {
        this.actions.addAll(Arrays.asList(actions));
        // Add the new action on to the tail so they are in order.
        this.actions.add(action);

        if (this.actions.size() > MAX_SEQUENCE_SIZE) {
            // Remove the head of the list.
            this.actions = this.actions.subList(1, MAX_SEQUENCE_SIZE + 1);
        }
    }

    public Sequence addAction(AbilityAction action) {
        return new Sequence(action, actions.toArray(new AbilityAction[actions.size()]));
    }

    public boolean matches(Sequence other) {
        // Return false if the current object has fewer entries than the other one
        if (actions.size() < other.actions.size()) {
            return false;
        }

        int max = Math.min(actions.size(), other.actions.size());

        for (int i = 0; i < max; ++i) {
            // Check for matches backward so the latest actions are included.
            AbilityAction first = actions.get(actions.size() - 1 - i);
            AbilityAction second = other.actions.get(other.actions.size() - 1 - i);

            if (!first.equals(second)) {
                return false;
            }
        }

        return true;
    }

    public int size() {
        return actions.size();
    }

    public List<AbilityAction> getActions() {
        return actions;
    }
}
