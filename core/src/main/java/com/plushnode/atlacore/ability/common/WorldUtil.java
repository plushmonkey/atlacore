package com.plushnode.atlacore.ability.common;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public final class WorldUtil {
    private WorldUtil() {

    }

    public static Collection<Block> getNearbyBlocks(Location location, double radius) {
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
                        blocks.add(location.getWorld().getBlockAt(x, y, z));
                    }
                }
            }
        }

        return blocks;
    }
}
