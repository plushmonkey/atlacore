package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.config.Configuration;

public interface RemovalPolicy {
    boolean shouldRemove();

    default void load(Configuration config) { }
    String getName();
}

