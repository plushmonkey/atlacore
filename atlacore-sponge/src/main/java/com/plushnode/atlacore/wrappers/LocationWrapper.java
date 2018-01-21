package com.plushnode.atlacore.wrappers;

import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

// Immutable implementation of Location
public class LocationWrapper implements Location {
    private org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location;

    public LocationWrapper(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location) {
        this.location = location;
    }

    public org.spongepowered.api.world.Location<org.spongepowered.api.world.World> getSpongeLocation() {
        return location;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocationWrapper) {
            return location.equals(((LocationWrapper)obj).location);
        }
        return location.equals(obj);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public String toString() {
        return location.toString();
    }

    @Override
    public Location add(double x, double y, double z) {
        return new LocationWrapper(location.add(x, y, z));
    }

    @Override
    public Location add(Location vec) {
        Vector3d vector = SpongeTypeUtil.toVector(((LocationWrapper)vec).getSpongeLocation());
        return new LocationWrapper(location.add(vector));
    }

    @Override
    public Location add(Vector3D vec) {
        return new LocationWrapper(location.add(SpongeTypeUtil.adapt(vec)));
    }

    @Override
    public Location clone() {
        return new LocationWrapper(location.copy());
    }

    @Override
    public double distance(Location o) {
        return SpongeTypeUtil.toVector(location).distance(SpongeTypeUtil.toVector(o));
    }

    @Override
    public double distanceSquared(Location o) {
        return SpongeTypeUtil.toVector(location).distanceSquared(SpongeTypeUtil.toVector(o));
    }

    @Override
    public Block getBlock() {
        return new BlockWrapper(location);
    }

    @Override
    public int getBlockX() {
        return location.getBlockX();
    }

    @Override
    public int getBlockY() {
        return location.getBlockY();
    }

    @Override
    public int getBlockZ() {
        return location.getBlockZ();
    }

    @Override
    public World getWorld() {
        return new WorldWrapper(location.getExtent());
    }

    @Override
    public double getX() {
        return location.getX();
    }

    @Override
    public double getY() {
        return location.getY();
    }

    @Override
    public double getZ() {
        return location.getZ();
    }

    @Override
    public double length() {
        return SpongeTypeUtil.toVector(location).length();
    }

    @Override
    public double lengthSquared() {
        return SpongeTypeUtil.toVector(location).lengthSquared();
    }

    @Override
    public Location setWorld(World world) {
        org.spongepowered.api.world.World spongeWorld = ((WorldWrapper)world).getSpongeWorld();

        return new LocationWrapper(location.setExtent(spongeWorld));
    }

    @Override
    public Location setX(double x) {
        Vector3d p = location.getPosition();
        Vector3d np = new Vector3d(x, p.getY(), p.getZ());
        return new LocationWrapper(location.setPosition(np));
    }

    @Override
    public Location setY(double y) {
        Vector3d p = location.getPosition();
        Vector3d np = new Vector3d(p.getX(), y, p.getZ());
        return new LocationWrapper(location.setPosition(np));
    }

    @Override
    public Location setZ(double z) {
        Vector3d p = location.getPosition();
        Vector3d np = new Vector3d(p.getX(), p.getY(), z);
        return new LocationWrapper(location.setPosition(np));
    }

    @Override
    public Location subtract(double x, double y, double z) {
        return new LocationWrapper(location.sub(x, y, z));
    }

    @Override
    public Location subtract(Location loc) {
        return new LocationWrapper(location.sub(SpongeTypeUtil.toVector(loc)));
    }

    @Override
    public Location subtract(Vector3D vec) {
        return new LocationWrapper(location.sub(SpongeTypeUtil.adapt(vec)));
    }

    @Override
    public Vector3D toVector() {
        return new Vector3D(location.getX(), location.getY(), location.getZ());
    }
}
