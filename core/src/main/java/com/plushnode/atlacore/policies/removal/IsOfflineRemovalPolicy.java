package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.Player;
import com.plushnode.atlacore.User;

public class IsOfflineRemovalPolicy implements RemovalPolicy {
    private User user;

    public IsOfflineRemovalPolicy(User user) {
        this.user = user;
    }

    @Override
    public boolean shouldRemove() {
        if (!(user instanceof Player)) return false;

        return !((Player)user).isOnline();
    }

    @Override
    public String getName() {
        return "IsOffline";
    }
}
