package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.Location;
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

}
