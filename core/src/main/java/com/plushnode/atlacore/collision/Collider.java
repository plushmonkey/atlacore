package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.platform.World;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Collider {
    boolean intersects(Collider collider);
    Vector3D getPosition();
    Vector3D getHalfExtents();
    World getWorld();

    boolean contains(Vector3D point);
}
