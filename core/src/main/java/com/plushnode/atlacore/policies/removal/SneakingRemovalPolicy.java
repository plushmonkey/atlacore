package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.platform.User;

public class SneakingRemovalPolicy implements RemovalPolicy {
    private User user;
    private boolean shouldSneak;

    public SneakingRemovalPolicy(User user, boolean shouldSneak) {
        this.user = user;
        this.shouldSneak = shouldSneak;
    }

    @Override
    public boolean shouldRemove() {
        return user.isSneaking() != shouldSneak;
    }

    @Override
    public String getName() {
        return "sneaking";
    }
}
