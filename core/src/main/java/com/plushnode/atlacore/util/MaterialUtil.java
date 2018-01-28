package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.collision.geometry.AABB;

import java.util.Arrays;
import java.util.List;

public final class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER, Material.LAVA,
            Material.STATIONARY_LAVA, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
            Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.CROPS, Material.SNOW, Material.SUGAR_CANE_BLOCK, Material.VINE, Material.DOUBLE_PLANT
    );

    private static final List<Material> EARTH_MATERIALS = Arrays.asList(
            Material.DIRT, Material.MYCEL, Material.GRASS, Material.STONE, Material.GRAVEL, Material.CLAY,
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE,
            Material.DIAMOND_ORE, Material.NETHERRACK, Material.QUARTZ_ORE, Material.COBBLESTONE, Material.STEP
    );

    private MaterialUtil() {

    }

    public static boolean isTransparent(Block block) {
        return TRANSPARENT_MATERIALS.contains(block.getType());
    }

    public static boolean isSolid(Block block) {
        AABB blockBounds = block.getBounds();

        // The block bounding box will have width if it's solid.
        if (blockBounds.min() == null || blockBounds.max() == null) return false;

        return blockBounds.min().distanceSq(blockBounds.max()) > 0;
    }

    public static boolean isEarthbendable(Block block) {
        return EARTH_MATERIALS.contains(block.getType());
    }

    public static boolean isLava(Block block) {
        Material type = block.getType();
        return type == Material.LAVA || type == Material.STATIONARY_LAVA;
    }
}
