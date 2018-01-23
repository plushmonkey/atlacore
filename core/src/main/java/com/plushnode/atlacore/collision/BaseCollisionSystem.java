package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public abstract class BaseCollisionSystem implements CollisionSystem {
    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    @Override
    public boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector3D extent = collider.getHalfExtents().add(new Vector3D(ExtentBuffer, ExtentBuffer, ExtentBuffer));
        Vector3D pos = collider.getPosition();
        Location location = user.getLocation().setX(pos.getX()).setY(pos.getY()).setZ(pos.getZ());

        boolean hit = false;

        for (Entity entity : location.getWorld().getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (entity.equals(user)) continue;

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
}
