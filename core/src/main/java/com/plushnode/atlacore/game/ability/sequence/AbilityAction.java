package com.plushnode.atlacore.game.ability.sequence;

import com.plushnode.atlacore.game.ability.AbilityDescription;

public class AbilityAction {
    private AbilityDescription abilityDescription;
    private Action action;

    public AbilityAction(AbilityDescription abilityDescription, Action action) {
        this.abilityDescription = abilityDescription;
        this.action = action;
    }

    public AbilityDescription getAbilityDescription() {
        return this.abilityDescription;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AbilityAction) {
            AbilityAction otherAction = ((AbilityAction)other);
            return action == otherAction.action && abilityDescription == otherAction.abilityDescription;
        }

        return super.equals(other);
    }
}
