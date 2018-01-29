package com.plushnode.atlacore.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public final class VectorUtil {
    private VectorUtil() {

    }

    public static double getEntry(Vector3D vec, int axis) {
        if (axis == 0) return vec.getX();
        if (axis == 1) return vec.getY();
        return vec.getZ();
    }

    public static Vector3D setX(Vector3D vec, double x) {
        return new Vector3D(x, vec.getY(), vec.getZ());
    }

    public static Vector3D setY(Vector3D vec, double y) {
        return new Vector3D(vec.getX(), y, vec.getZ());
    }

    public static Vector3D setZ(Vector3D vec, double z) {
        return new Vector3D(vec.getX(), vec.getY(), z);
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

    public static Vector3D getBlockVector(Vector3D vector) {
        return new Vector3D(Math.floor(vector.getX()), Math.floor(vector.getY()), Math.floor(vector.getZ()));
    }

    public static double getMaxComponent(Vector3D vector) {
        return Math.max(vector.getX(), Math.max(vector.getY(), vector.getZ()));
    }

    public static Vector3D rotate(Vector3D vector, Vector3D axis, double rads) {
        Vector3D a = vector.scalarMultiply(Math.cos(rads));
        Vector3D b = axis.crossProduct(vector).scalarMultiply(Math.sin(rads));
        Vector3D c = axis.scalarMultiply(axis.dotProduct(vector)).scalarMultiply(1 - Math.cos(rads));

        return a.add(b).add(c);
    }
}
