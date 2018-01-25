package com.plushnode.atlacore.util;

import com.plushnode.atlacore.collision.AABB;
import com.plushnode.atlacore.collision.Ray;
import com.plushnode.atlacore.game.Game;
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

            AABB entityBounds = Game.getCollisionSystem().getAABB(entity).at(entity.getLocation());

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

}
