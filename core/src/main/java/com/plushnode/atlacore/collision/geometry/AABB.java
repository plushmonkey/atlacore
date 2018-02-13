package com.plushnode.atlacore.collision.geometry;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

public class AABB implements Collider {
    public static final AABB PLAYER_BOUNDS = new AABB(new Vector3D(-0.3, 0.0, -0.3), new Vector3D(0.3, 1.8, 0.3));
    public static final AABB BLOCK_BOUNDS = new AABB(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 1.0, 1.0));

    private Vector3D min;
    private Vector3D max;

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    public AABB at(Vector3D pos) {
        if (min == null || max == null) return new AABB(null, null);

        return new AABB(min.add(pos), max.add(pos));
    }

    public AABB at(Location location) {
        if (min == null || max == null) return new AABB(null, null);

        return at(location.toVector());
    }

    public AABB grow(double x, double y, double z) {
        Vector3D change = new Vector3D(x, y, z);

        return new AABB(min.subtract(change), max.add(change));
    }

    public AABB scale(double x, double y, double z) {
        Vector3D extents = getHalfExtents();
        Vector3D newExtents = VectorUtil.hadamard(extents, new Vector3D(x, y, z));
        Vector3D diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AABB scale(double amount) {
        Vector3D extents = getHalfExtents();
        Vector3D newExtents = extents.scalarMultiply(amount);
        Vector3D diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public Vector3D min() {
        return this.min;
    }

    public Vector3D max() {
        return this.max;
    }

    public Vector3D mid() {
        return this.min.add(this.max().subtract(this.min()).scalarMultiply(0.5));
    }

    public boolean contains(Vector3D test) {
        if (min == null || max == null) return false;

        return (test.getX() >= min.getX() && test.getX() <= max.getX()) &&
                (test.getY() >= min.getY() && test.getY() <= max.getY()) &&
                (test.getZ() >= min.getZ() && test.getZ() <= max.getZ());
    }

    public Optional<Double> intersects(Ray ray) {
        if (min == null || max == null) return Optional.empty();

        double t1 = (min.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();
        double t2 = (max.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();

        double t3 = (min.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();
        double t4 = (max.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();

        double t5 = (min.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();
        double t6 = (max.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax) {
            return Optional.empty();
        }

        return Optional.of(tmin);
    }

    public boolean intersects(AABB other) {
        if (min == null || max == null || other.min == null || other.max == null) {
            return false;
        }

        return (max.getX() > other.min.getX() &&
                min.getX() < other.max.getX() &&
                max.getY() > other.min.getY() &&
                min.getY() < other.max.getY() &&
                max.getZ() > other.min.getZ() &&
                min.getZ() < other.max.getZ());
    }

    public boolean intersects(Sphere sphere) {
        return sphere.intersects(this);
    }

    @Override
    public boolean intersects(Collider collider) {
        if (collider instanceof Sphere) {
            return intersects((Sphere) collider);
        } else if (collider instanceof AABB) {
            return intersects((AABB) collider);
        } else if (collider instanceof OBB) {
            return collider.intersects(this);
        } else if (collider instanceof Disc) {
            return collider.intersects(this);
        }

        return false;
    }

    @Override
    public Vector3D getPosition() {
        return mid();
    }

    @Override
    public Vector3D getHalfExtents() {
        if (max == null || min == null) return new Vector3D(0, 0, 0);

        Vector3D half = max.subtract(min).scalarMultiply(0.5);
        return new Vector3D(Math.abs(half.getX()), Math.abs(half.getY()), Math.abs(half.getZ()));
    }

    @Override
    public String toString() {
        return "[AABB min: " + min + ", max: " + max + "]";
    }
}
