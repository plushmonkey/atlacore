package com.plushnode.atlacore.ability.common;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class WorldUtil {
    private WorldUtil() {

    }

    public static List<Block> getNearbyBlocks(Location location, double radius) {
        List<Block> blocks = new ArrayList<>();
        int r = (int)radius + 1;

        int originX = (int)Math.floor(location.getX());
        int originY = (int)Math.floor(location.getY());
        int originZ = (int)Math.floor(location.getZ());

        for (int x = originX - r; x < originX + r; ++x) {
            for (int y = originY - r; y < originY + r; ++y) {
                for (int z = originZ - r; z < originZ + r; ++z) {
                    Block check = location.getWorld().getBlockAt(x, y, z);

                    if (check.getLocation().distanceSquared(location) <= radius * radius) {
                        blocks.add(location.getBlock());
                    }
                }
            }
        }

        return blocks;
    }
}
