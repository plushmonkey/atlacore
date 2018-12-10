package com.plushnode.atlacore.collision.geometry;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.World;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

// Combines an OBB and Sphere to create a disc-like collider.
public class Disc implements Collider {
    private Sphere sphere;
    private OBB obb;

    public Disc(OBB obb, Sphere sphere) {
        this.obb = obb;
        this.sphere = sphere;
    }

    public Disc addPosition(Vector3D position) {
        return new Disc(this.obb.addPosition(position), this.sphere.at(position));
    }

    public Disc addPosition(Location position) {
        return new Disc(this.obb.addPosition(position), this.sphere.at(position));
    }

    public Disc at(Vector3D position) {
        return new Disc(this.obb.at(position), this.sphere.at(position));
    }

    public Disc at(Location position) {
        return new Disc(this.obb.at(position), this.sphere.at(position));
    }

    @Override
    public boolean intersects(Collider collider) {
        return sphere.intersects(collider) && obb.intersects(collider);
    }

    @Override
    public Vector3D getPosition() {
        return sphere.center;
    }

    @Override
    public Vector3D getHalfExtents() {
        return obb.getHalfExtents();
    }

    @Override
    public World getWorld() {
        return obb.getWorld();
    }

    @Override
    public boolean contains(Vector3D point) {
        return sphere.contains(point) && obb.contains(point);
    }

    public OBB getOBB() {
        return obb;
    }

    public Sphere getSphere() {
        return sphere;
    }
}
