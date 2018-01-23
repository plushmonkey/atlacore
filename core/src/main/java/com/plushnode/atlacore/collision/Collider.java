package com.plushnode.atlacore.collision;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Collider {
    boolean intersects(AABB aabb);
    boolean intersects(Sphere sphere);
    Vector3D getPosition();
    Vector3D getHalfExtents();

    boolean contains(Vector3D point);
}
