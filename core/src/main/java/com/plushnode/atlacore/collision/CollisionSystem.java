package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.Entity;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.Material;

import java.util.Set;

public interface CollisionSystem {
    boolean isOnGround(Entity entity);
    double distanceAboveGround(Entity entity);
    double distanceAboveGround(Entity entity, Set<Material> groundMaterials);

    AABB getAABB(Entity entity);
    AABB getAABB(Block block);
}
