package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.Entity;
import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.World;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.Material;

import java.util.Set;

public interface CollisionSystem {
    boolean isOnGround(Entity entity);
    double distanceAboveGround(Entity entity);
    double distanceAboveGround(Entity entity, Set<Material> groundMaterials);

    AABB getAABB(Entity entity);
    AABB getAABB(Block block);

    boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly);
    boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly, boolean selfCollisions);

    // Returns a location of intersection for a ray casted from the origin.
    // Returns a location at the maxRange if no intersections were found.
    Location castRay(World world, Ray ray, double maxRange);
}
