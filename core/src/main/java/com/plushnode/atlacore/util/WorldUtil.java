package com.plushnode.atlacore.util;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public final class WorldUtil {
    private WorldUtil() {

    }

    public static Collection<Block> getNearbyBlocks(Location location, double radius) {
        return getNearbyBlocks(location, radius, Collections.emptyList());
    }

    public static Collection<Block> getNearbyBlocks(Location location, double radius, List<Material> ignoreMaterials) {
        int r = (int)radius + 1;

        int originX = (int)Math.floor(location.getX());
        int originY = (int)Math.floor(location.getY());
        int originZ = (int)Math.floor(location.getZ());

        Set<Block> blocks = new HashSet<>();
        Vector3D pos = location.toVector();

        for (int x = originX - r; x < originX + r; ++x) {
            for (int y = originY - r; y < originY + r; ++y) {
                for (int z = originZ - r; z < originZ + r; ++z) {
                    if (pos.distanceSq(new Vector3D(x, y, z)) <= radius * radius) {
                        Block block = location.getWorld().getBlockAt(x, y, z);

                        if (!ignoreMaterials.contains(block.getType())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }

        return blocks;
    }

    public static LivingEntity getTargetEntity(User user, int range) {
        Ray ray = new Ray(user.getEyeLocation(), user.getDirection());

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : user.getWorld().getNearbyEntities(user.getLocation(), range, range, range)) {
            if (entity.equals(user)) continue;
            if (!(entity instanceof LivingEntity)) continue;

            AABB entityBounds = entity.getBounds().at(entity.getLocation());

            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                double dist = result.get();

                if (dist < closestDist) {
                    closest = (LivingEntity)entity;
                    closestDist = dist;
                }
            }
        }

        if (closestDist > range) {
            return null;
        }

        return closest;
    }


    public static boolean isOnGround(Entity entity) {
        final double epsilon = 0.01;

        Location location = entity.getLocation();
        AABB entityBounds = entity.getBounds().at(location.subtract(0, epsilon, 0));

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                Block checkBlock = location.clone().add(x, -epsilon, z).getBlock();
                if (checkBlock.getType() == Material.AIR) continue;

                AABB checkBounds = checkBlock.getBounds().at(checkBlock.getLocation());

                if (entityBounds.intersects(checkBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static double distanceAboveGround(Entity entity) {
        return distanceAboveGround(entity, Collections.emptySet());
    }

    public static double distanceAboveGround(Entity entity, Set<Material> groundMaterials) {
        Location location = entity.getLocation();
        Ray ray = new Ray(location, new Vector3D(0, -1, 0));

        for (double y = location.getY() - 1; y >= 0; --y) {
            location = location.setY(y);

            Block block = location.getBlock();
            AABB checkBounds;

            if (groundMaterials.contains(block.getType())) {
                checkBounds = AABB.BLOCK_BOUNDS;
            } else {
                checkBounds = block.getBounds();
            }

            checkBounds = checkBounds.at(block.getLocation());

            Optional<Double> rayHit = checkBounds.intersects(ray);

            if (rayHit.isPresent()) {
                return rayHit.get();
            }
        }

        return Double.MAX_VALUE;
    }
}
