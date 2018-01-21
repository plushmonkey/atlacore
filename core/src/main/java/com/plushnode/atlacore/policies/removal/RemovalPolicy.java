package com.plushnode.atlacore.policies.removal;

import ninja.leaping.configurate.ConfigurationNode;

public interface RemovalPolicy {
    boolean shouldRemove();

    default void load(ConfigurationNode config) { }
    String getName();
}

