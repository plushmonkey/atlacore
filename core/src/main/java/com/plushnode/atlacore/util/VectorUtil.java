package com.plushnode.atlacore.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public final class VectorUtil {
    private VectorUtil() {

    }

    public static Vector3D clearAxis(Vector3D vec, int axis) {
        if (axis == 0) {
            return new Vector3D(0, vec.getY(), vec.getZ());
        } else if (axis == 1) {
            return new Vector3D(vec.getX(), 0, vec.getZ());
        } else {
            return new Vector3D(vec.getX(), vec.getY(), 0);
        }
    }

    public static Vector3D getAxisVector(Vector3D vec, int axis) {
        if (axis == 0) {
            return new Vector3D(vec.getX(), 0, 0);
        } else if (axis == 1) {
            return new Vector3D(0, vec.getY(), 0);
        } else {
            return new Vector3D(0, 0, vec.getZ());
        }
    }
}
