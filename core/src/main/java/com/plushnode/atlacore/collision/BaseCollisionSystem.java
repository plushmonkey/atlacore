package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.ability.common.WorldUtil;
import com.plushnode.atlacore.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BaseCollisionSystem implements CollisionSystem {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.ZERO, Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_K,
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K
    );

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
    public Location castRay(World world, Ray ray, double maxRange) {
        Collection<Block> blocks = WorldUtil.getNearbyBlocks(world.getLocation(ray.origin), maxRange + 1);

        double closestDistance = Double.MAX_VALUE;

        for (Block block : blocks) {
            AABB blockBounds = Game.getCollisionSystem().getAABB(block).at(block.getLocation());

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
}
