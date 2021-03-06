package com.plushnode.atlacore.util;

import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.material.SpongeMaterialUtil;
import com.plushnode.atlacore.platform.ItemStack;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.LocationWrapper;
import com.plushnode.atlacore.platform.block.Material;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class SpongeTypeUtil {
    // Note: these must be in the order of core BlockFace order.
    private static final Direction[] DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,
            Direction.UP, Direction.DOWN,
            Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST,
            Direction.WEST_NORTHWEST, Direction.NORTH_NORTHWEST, Direction.NORTH_NORTHEAST, Direction.EAST_NORTHEAST,
            Direction.EAST_SOUTHEAST, Direction.SOUTH_SOUTHEAST, Direction.SOUTH_SOUTHWEST, Direction.WEST_SOUTHWEST,
            Direction.NONE
    };

    private SpongeTypeUtil() {

    }

    public static Vector3d adapt(Vector3D vec) {
        return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3D adapt(Vector3d vec) {
        return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
    }

    public static com.plushnode.atlacore.platform.Location adapt(Location<World> loc) {
        return new LocationWrapper(loc);
    }

    public static Location<World> adapt(com.plushnode.atlacore.platform.Location loc) {
        return ((LocationWrapper)loc).getSpongeLocation();
    }

    public static Vector3d toVector(com.plushnode.atlacore.platform.Location location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public static Vector3d toVector(Location<World> location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public static Direction adapt(BlockFace face) {
        return DIRECTIONS[face.ordinal()];
    }

    public static Material adapt(ItemType type) {
        return SpongeMaterialUtil.toMaterial(type);
    }

    public static Material adapt(BlockType type) {
        return SpongeMaterialUtil.toMaterial(type);
    }

    public static ItemStack adapt(org.spongepowered.api.item.inventory.ItemStack item) {
        if (item == null) return null;
        return new ItemStack(adapt(item.getType()), item.getQuantity());
    }

    public static org.spongepowered.api.item.inventory.ItemStack adapt(ItemStack item) {
        if (item == null) return null;

        ItemType type = SpongeMaterialUtil.toItemType(item.getType());

        if (type == null) {
            return null;
        }

        return org.spongepowered.api.item.inventory.ItemStack.builder()
                .itemType(type)
                .quantity(item.getAmount())
                .build();
    }
}
