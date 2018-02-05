package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.function.Supplier;

public class OutOfRangeRemovalPolicy implements RemovalPolicy {
    private Supplier<Location> fromSupplier;
    private User user;
    private double range;

    public OutOfRangeRemovalPolicy(User user, double range, Supplier<Location> from) {
        this.user = user;
        this.range = range;
        this.fromSupplier = from;
    }

    @Override
    public boolean shouldRemove() {
        if (this.range == 0) return false;

        Location from = this.fromSupplier.get();

        if (!from.getWorld().equals(user.getWorld())) return true;

        return from.distanceSquared(this.user.getLocation()) >= (this.range * this.range);
    }

    @Override
    public void load(ConfigurationNode config) {
        this.range = config.getNode("range").getDouble();
    }

    @Override
    public String getName() {
        return "OutOfRange";
    }
}
