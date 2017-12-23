package com.plushnode.atlacore.util;

import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.collision.AABB;

import java.util.Arrays;
import java.util.List;

public final class MaterialUtil {
    private static final List<Integer> TRANSPARENT_MATERIALS = Arrays.asList(0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106, 175);

    private MaterialUtil() {

    }

    public static boolean isTransparent(Block block) {
        return TRANSPARENT_MATERIALS.contains(block.getTypeId());
    }

    public static boolean isSolid(Block block) {
        AABB blockBounds = Game.getCollisionSystem().getAABB(block);

        // The block bounding box will have width if it's solid.
        if (blockBounds.min() == null || blockBounds.max() == null) return false;

        return blockBounds.min().distanceSq(blockBounds.max()) > 0;
    }
}
