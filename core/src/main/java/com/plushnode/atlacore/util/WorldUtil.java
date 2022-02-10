package com.plushnode.atlacore.util;

import com.plushnode.atlacore.collision.RayCaster;
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

    public static Collection<Block> getNearbyBlocks(Location location, double radius, Material... ignoreMaterials) {
        return getNearbyBlocks(location, radius, Arrays.asList(ignoreMaterials));
    }

    public static Collection<Block> getNearbyBlocks(Location location, double radius, List<Material> ignoreMaterials) {
        int r = (int)radius + 2;

        double originX = location.getX();
        double originY = location.getY();
        double originZ = location.getZ();

        List<Block> blocks = new ArrayList<>();
        Vector3D pos = location.toVector();

        for (double x = originX - r; x <= originX + r; ++x) {
            for (double y = originY - r; y <= originY + r; ++y) {
                for (double z = originZ - r; z <= originZ + r; ++z) {
                    if (pos.distanceSq(new Vector3D(x, y, z)) <= radius * radius) {
                        Block block = location.getWorld().getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));

                        if (!ignoreMaterials.contains(block.getType())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }

        return blocks;
    }

    public static Collection<Block> getNearbyType(Location location, double radius, Material type) {
        int r = (int)radius + 2;

        double originX = location.getX();
        double originY = location.getY();
        double originZ = location.getZ();

        List<Block> blocks = new ArrayList<>();
        Vector3D pos = location.toVector();

        for (double x = originX - r; x <= originX + r; ++x) {
            for (double y = originY - r; y <= originY + r; ++y) {
                for (double z = originZ - r; z <= originZ + r; ++z) {
                    if (pos.distanceSq(new Vector3D(x, y, z)) <= radius * radius) {
                        Block block = location.getWorld().getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));

                        if (block.getType() == type) {
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
                if (MaterialUtil.isAir(checkBlock.getType())) continue;

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

    public static double distanceAboveGround(Entity entity, Material... materials) {
        return distanceAboveGround(entity, new HashSet<>(Arrays.asList(materials)));
    }

    public static double distanceAboveGround(Entity entity, Set<Material> groundMaterials) {
        Location location = entity.getLocation();
        Ray ray = new Ray(location, new Vector3D(0, -1, 0));

        for (double y = location.getY() - 1; y >= -64; --y) {
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

    public static boolean canView(User user, Location location, double maxRange) {
        return canView(user, location.getBlock(), maxRange);
    }

    public static boolean canView(User user, Block block, double maxRange) {
        if (!user.getWorld().equals(block.getWorld())) return false;

        Vector3D direction =  block.getLocation().subtract(user.getEyeLocation()).toVector().normalize();
        Ray viewRay = new Ray(user.getEyeLocation(), direction);
        Block viewBlock = RayCaster.blockCast(user.getWorld(), viewRay, maxRange, block.getType() == Material.WATER);

        return block.equals(viewBlock);
    }
}
