package com.plushnode.atlacore.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public final class TypeUtil {
    private TypeUtil() {

    }

    public static Material adapt(com.plushnode.atlacore.block.Material material) {
        return Material.values()[material.ordinal()];
    }

    public static com.plushnode.atlacore.block.Material adapt(Material material) {
        return com.plushnode.atlacore.block.Material.values()[material.ordinal()];
    }

    public static Vector adapt(Vector3D vec) {
        return new Vector(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3D adapt(Vector vec) {
        return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
    }
}
