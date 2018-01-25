package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public abstract class BaseCollisionSystem implements CollisionSystem {
    @Override
    public boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly) {
        return handleEntityCollisions(user, collider, callback, livingOnly, false);
    }
    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    @Override
    public boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly, boolean selfCollision) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector3D extent = collider.getHalfExtents().add(new Vector3D(ExtentBuffer, ExtentBuffer, ExtentBuffer));
        Vector3D pos = collider.getPosition();
        Location location = user.getLocation().setX(pos.getX()).setY(pos.getY()).setZ(pos.getZ());

        boolean hit = false;

        for (Entity entity : location.getWorld().getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (!selfCollision && entity.equals(user)) continue;

            if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            if (livingOnly && !(entity instanceof LivingEntity)) {
                continue;
            }

            AABB entityBounds = Game.getCollisionSystem().getAABB(entity).at(entity.getLocation());
            if (collider.intersects(entityBounds)) {
                if (callback.onCollision(entity)) {
                    return true;
                }

                hit = true;
            }
        }

        return hit;
    }

    @Override
    public Location castRay(World world, Ray ray, double maxRange, boolean liquidCollision) {
        Collection<Block> blocks = WorldUtil.getNearbyBlocks(world.getLocation(ray.origin), maxRange + 1);

        double closestDistance = Double.MAX_VALUE;

        for (Block block : blocks) {
            AABB localBounds = Game.getCollisionSystem().getAABB(block);
            if (liquidCollision && block.isLiquid()) {
                localBounds = AABB.BLOCK_BOUNDS;
            }

            AABB blockBounds = localBounds.at(block.getLocation());

            Optional<Double> result = blockBounds.intersects(ray);
            if (result.isPresent()) {
                double distance = result.get();
                if (distance < closestDistance) {
                    closestDistance = distance;
                }
            }
        }

        closestDistance = Math.min(closestDistance, maxRange);

        return world.getLocation(ray.origin).add(ray.direction.scalarMultiply(closestDistance));
    }

    @Override
    public boolean collidesWithBlocks(World world, Collider collider, Location begin, Location end, boolean liquids) {
        double maxExtent = VectorUtil.getMaxComponent(collider.getHalfExtents());
        double distance = begin.distance(end);

        Vector3D toEnd = end.subtract(begin).toVector().normalize();
        Ray ray = new Ray(begin.toVector(), toEnd);

        Location mid = begin.add(toEnd.scalarMultiply(distance / 2.0));
        double lookupRadius = (distance / 2.0) + maxExtent + 1.0;

        for (Block block : WorldUtil.getNearbyBlocks(mid, lookupRadius, Collections.singletonList(Material.AIR))) {
            AABB localBounds = Game.getCollisionSystem().getAABB(block);

            if (liquids && block.isLiquid()) {
                localBounds = AABB.BLOCK_BOUNDS;
            }

            AABB blockBounds = localBounds.at(block.getLocation());

            Optional<Double> result = blockBounds.intersects(ray);
            if (result.isPresent()) {
                double d = result.get();
                if (d < distance) {
                    return true;
                }
            }
        }

        return false;
    }
}
