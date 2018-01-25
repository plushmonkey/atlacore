package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;

public class SwappedSlotsRemovalPolicy<T extends Ability> implements RemovalPolicy {
    private User user;
    private Class<? extends T> type;

    public SwappedSlotsRemovalPolicy(User user, Class<? extends T> type) {
        this.user = user;
        this.type = type;
    }

    @Override
    public boolean shouldRemove() {
        if (!(user instanceof Player)) return false;

        AbilityDescription desc = user.getSelectedAbility();
        return desc == null || !desc.createAbility().getClass().equals(type);
    }

    @Override
    public String getName() {
        return "SwappedSlots";
    }
}
