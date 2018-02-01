package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.World;
import com.plushnode.atlacore.platform.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public final class RayCaster {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_J,
            Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_J,

            new Vector3D(0, 1, 1), new Vector3D(0, 1, -1),
            new Vector3D(1, 1, 0), new Vector3D(1, 1, 1), new Vector3D(1, 1, -1),
            new Vector3D(-1, 1, 0), new Vector3D(-1, 1, 1), new Vector3D(-1, 1, -1),

            new Vector3D(0, -1, 1), new Vector3D(0, -1, -1),
            new Vector3D(1, -1, 0), new Vector3D(1, -1, 1), new Vector3D(1, -1, -1),
            new Vector3D(-1, -1, 0), new Vector3D(-1, -1, 1), new Vector3D(-1, -1, -1)
    );

    private RayCaster() {

    }

    public static Location cast(User user, Ray ray, double maxRange, boolean liquidCollision, boolean entityCollision) {
        World world = user.getWorld();
        Location location = cast(world, ray, maxRange, liquidCollision);

        if (!entityCollision) {
            return location;
        }

        int radius = (int)maxRange + 3;
        Location start = world.getLocation(ray.origin);
        double closestDistance = location.subtract(start).length();

        for (Entity entity : world.getNearbyEntities(start, radius, radius, radius)) {
            if (entity.equals(user)) continue;

            AABB entityBounds = entity.getBounds().at(entity.getLocation());
            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                double distance = result.get();

                if (distance < closestDistance) {
                    closestDistance = distance;
                }
            }
        }

        return start.add(ray.direction.scalarMultiply(closestDistance));
    }

    public static Location cast(World world, Ray ray, double maxRange, boolean liquidCollision) {
        Location origin = world.getLocation(ray.origin);
        double closestDistance = Double.MAX_VALUE;

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < maxRange + 1; ++i) {
            Location current = origin.add(ray.direction.scalarMultiply(i));
            for (Vector3D direction : DIRECTIONS) {
                Location check = current.add(direction);
                Block block = check.getBlock();
                AABB localBounds = block.getBounds();

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

            // Break early after checking all neighbors for intersection.
            if (closestDistance < maxRange) {
                break;
            }
        }

        closestDistance = Math.min(closestDistance, maxRange);
        return origin.add(ray.direction.scalarMultiply(closestDistance));
    }
}
