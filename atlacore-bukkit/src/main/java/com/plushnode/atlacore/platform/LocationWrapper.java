package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.util.TypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

// This is an immutable wrapper around Bukkit's mutable location.
public class LocationWrapper implements Location {
    private org.bukkit.Location location;

    public LocationWrapper(org.bukkit.Location location) {
        this.location = location.clone();
    }

    public org.bukkit.Location getBukkitLocation() {
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
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.getBukkitLocation().add(x, y, z);
        return newLoc;
    }

    @Override
    public Location add(Location vec) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.getBukkitLocation().add(TypeUtil.adapt(vec));
        return newLoc;
    }

    @Override
    public Location add(Vector3D vec) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.getBukkitLocation().add(TypeUtil.adapt(vec));
        return newLoc;
    }

    @Override
    public Location clone() {
        return new LocationWrapper(location);
    }

    @Override
    public double distance(Location o) {
        LocationWrapper wrapper = (LocationWrapper)o;

        return location.distance(wrapper.getBukkitLocation());
    }

    @Override
    public double distanceSquared(Location o) {
        LocationWrapper wrapper = (LocationWrapper)o;

        return location.distanceSquared(wrapper.getBukkitLocation());
    }

    @Override
    public Block getBlock() {
        return new BlockWrapper(location.getBlock());
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
        return new WorldWrapper(location.getWorld());
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
        return location.length();
    }

    @Override
    public double lengthSquared() {
        return location.lengthSquared();
    }

    @Override
    public Location setWorld(World world) {
        LocationWrapper newLoc = new LocationWrapper(location);

        WorldWrapper wrapper = (WorldWrapper)world;
        newLoc.location.setWorld(wrapper.getBukkitWorld());

        return newLoc;
    }

    @Override
    public Location setX(double x) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.location.setX(x);
        return newLoc;
    }

    @Override
    public Location setY(double y) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.location.setY(y);
        return newLoc;
    }

    @Override
    public Location setZ(double z) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.location.setZ(z);
        return newLoc;
    }

    @Override
    public Location subtract(double x, double y, double z) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.location = newLoc.location.subtract(x, y, z);
        return newLoc;
    }

    @Override
    public Location subtract(Location loc) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.location = newLoc.location.subtract(TypeUtil.adapt(loc));
        return newLoc;
    }

    @Override
    public Location subtract(Vector3D vec) {
        LocationWrapper newLoc = new LocationWrapper(location);
        newLoc.location = newLoc.location.subtract(TypeUtil.adapt(vec));
        return newLoc;
    }

    @Override
    public Vector3D toVector() {
        return new Vector3D(location.getX(), location.getY(), location.getZ());
    }
}
