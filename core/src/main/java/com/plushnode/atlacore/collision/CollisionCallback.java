package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.Entity;

public interface CollisionCallback {
    boolean onCollision(Entity e);
}
