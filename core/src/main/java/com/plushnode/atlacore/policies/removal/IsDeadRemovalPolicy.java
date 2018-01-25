package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;

public class IsDeadRemovalPolicy implements RemovalPolicy {
    private User user;

    public IsDeadRemovalPolicy(User user) {
        this.user = user;
    }

    @Override
    public boolean shouldRemove() {
        if (!(user instanceof Player)) return false;

        Player player = (Player) user;

        return !player.isOnline() || user.isDead();
    }

    @Override
    public String getName() {
        return "IsDead";
    }
}
