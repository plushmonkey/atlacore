package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.platform.Entity;

public interface CollisionCallback {
    boolean onCollision(Entity e);
}
