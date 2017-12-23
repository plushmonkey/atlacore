package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.Location;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

public interface AABB {
    AABB at(Vector3D pos);
    AABB at(Location location);

    Vector3D min();
    Vector3D max();
    Vector3D mid();
    boolean contains(Vector3D test);
    Optional<Double> intersects(Ray ray);
    boolean intersects(AABB other);
}
