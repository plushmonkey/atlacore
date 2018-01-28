package com.plushnode.atlacore.collision.geometry;

import com.plushnode.atlacore.collision.Collider;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Sphere implements Collider {
    public Vector3D center;
    public double radius;

    public Sphere(Vector3D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Sphere at(Vector3D newCenter) {
        return new Sphere(newCenter, radius);
    }

    public boolean intersects(AABB aabb) {
        Vector3D min = aabb.min();
        Vector3D max = aabb.max();

        if (min == null || max == null) return false;

        // Get the point closest to sphere center on the aabb.
        double x = Math.max(min.getX(), Math.min(center.getX(), max.getX()));
        double y = Math.max(min.getY(), Math.min(center.getY(), max.getY()));
        double z = Math.max(min.getZ(), Math.min(center.getZ(), max.getZ()));

        // Check if that point is inside of the sphere.
        return contains(new Vector3D(x, y, z));
    }

    public boolean intersects(Sphere other) {
        double distSq = other.center.distanceSq(center);
        double rsum = radius + other.radius;

        // Spheres will be colliding if their distance apart is less than the sum of the radii.
        return distSq <= rsum * rsum;
    }

    @Override
    public Vector3D getPosition() {
        return center;
    }

    @Override
    public Vector3D getHalfExtents() {
        return new Vector3D(radius, radius, radius);
    }

    public boolean contains(Vector3D point) {
        double distSq = center.distanceSq(point);
        return distSq <= radius * radius;
    }
}
