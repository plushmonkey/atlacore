package com.plushnode.atlacore.util;

import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.block.BlockFace;
import com.plushnode.atlacore.wrappers.LocationWrapper;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
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

    public static com.plushnode.atlacore.Location adapt(Location<World> loc) {
        return new LocationWrapper(loc);
    }

    public static Location<World> adapt(com.plushnode.atlacore.Location loc) {
        return ((LocationWrapper)loc).getSpongeLocation();
    }

    public static Vector3d toVector(com.plushnode.atlacore.Location location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public static Vector3d toVector(Location<World> location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public static Direction adapt(BlockFace face) {
        return DIRECTIONS[face.ordinal()];
    }
}
