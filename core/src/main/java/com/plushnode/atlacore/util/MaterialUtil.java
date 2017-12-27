package com.plushnode.atlacore.util;

import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.collision.AABB;

import java.util.Arrays;
import java.util.List;

public final class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER, Material.LAVA,
            Material.STATIONARY_LAVA, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
            Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.CROPS, Material.SNOW, Material.SUGAR_CANE_BLOCK, Material.VINE, Material.DOUBLE_PLANT
    );

    private MaterialUtil() {

    }

    public static boolean isTransparent(Block block) {
        return TRANSPARENT_MATERIALS.contains(block.getType());
    }

    public static boolean isSolid(Block block) {
        AABB blockBounds = Game.getCollisionSystem().getAABB(block);

        // The block bounding box will have width if it's solid.
        if (blockBounds.min() == null || blockBounds.max() == null) return false;

        return blockBounds.min().distanceSq(blockBounds.max()) > 0;
    }
}
