package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.ItemStack;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.LocationWrapper;
import com.plushnode.atlacore.platform.block.data.Levelled;
import com.plushnode.atlacore.platform.data.LevelledWrapper;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

public final class TypeUtil {
    private TypeUtil() {

    }

    public static Material adapt(com.plushnode.atlacore.platform.block.Material material) {
        return Material.values()[material.ordinal()];
    }

    public static com.plushnode.atlacore.platform.block.Material adapt(Material material) {
        return com.plushnode.atlacore.platform.block.Material.values()[material.ordinal()];
    }

    public static Vector adapt(Vector3D vec) {
        if (vec == null) return null;
        return new Vector(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3D adapt(Vector vec) {
        if (vec == null) return null;
        return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
    }

    public static org.bukkit.Location adapt(Location loc) {
        return ((LocationWrapper)loc).getBukkitLocation();
    }

    public static Location adapt(org.bukkit.Location loc) {
        return new LocationWrapper(loc);
    }

    public static ItemStack adapt(org.bukkit.inventory.ItemStack item) {
        return new ItemStack(TypeUtil.adapt(item.getType()), item.getAmount());
    }

    public static org.bukkit.inventory.ItemStack adapt(ItemStack item) {
        return new org.bukkit.inventory.ItemStack(TypeUtil.adapt(item.getType()), item.getAmount());
    }

    public static BlockData adapt(com.plushnode.atlacore.platform.block.data.BlockData data) {
        if (data instanceof Levelled) {
            return ((LevelledWrapper) data).getLevelled();
        }

        return null;
    }

    public static com.plushnode.atlacore.platform.block.data.BlockData adapt(BlockData bukkitData) {
        if (bukkitData instanceof org.bukkit.block.data.Levelled) {
            return new LevelledWrapper((org.bukkit.block.data.Levelled)bukkitData);
        }
        return null;
    }
}
