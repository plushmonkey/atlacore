package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.World;

public class OutOfWorldRemovalPolicy implements RemovalPolicy {
    private User user;
    private World world;

    public OutOfWorldRemovalPolicy(User user) {
        this.user = user;
        this.world = user.getWorld();
    }

    @Override
    public boolean shouldRemove() {
        return !user.getWorld().equals(world);
    }

    @Override
    public String getName() {
        return "OutOfWorld";
    }
}
