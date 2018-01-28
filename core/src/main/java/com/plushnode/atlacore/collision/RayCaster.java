package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.World;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.util.WorldUtil;

import java.util.Collection;
import java.util.Optional;

public final class RayCaster {
    private RayCaster() {

    }

    public static Location cast(World world, Ray ray, double maxRange, boolean liquidCollision) {
        Collection<Block> blocks = WorldUtil.getNearbyBlocks(world.getLocation(ray.origin), maxRange + 1);

        double closestDistance = Double.MAX_VALUE;

        for (Block block : blocks) {
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

        closestDistance = Math.min(closestDistance, maxRange);

        return world.getLocation(ray.origin).add(ray.direction.scalarMultiply(closestDistance));
    }
}
