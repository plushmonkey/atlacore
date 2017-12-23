package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.util.TypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class LocationWrapper implements Location {
    private org.bukkit.Location location;

    public LocationWrapper(org.bukkit.Location location) {
        this.location = location;
    }

    public org.bukkit.Location getBukkitLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location.toString();
    }

    @Override
    public Location add(double x, double y, double z) {
        location = location.add(x, y, z);
        return this;
    }

    @Override
    public Location add(Location vec) {
        location = location.add(((LocationWrapper)vec).getBukkitLocation());
        return this;
    }

    @Override
    public Location add(Vector3D vec) {
        location = location.add(TypeUtil.adapt(vec));
        return this;
    }

    @Override
    public Location clone() {
        return new LocationWrapper(location.clone());
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
    public Vector3D getDirection() {
        return TypeUtil.adapt(location.getDirection());
    }

    @Override
    public float getPitch() {
        return location.getPitch();
    }

    @Override
    public float getYaw() {
        return location.getYaw();
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
    public Location multiply(double m) {
        location = location.multiply(m);
        return this;
    }

    @Override
    public Location setDirection(Vector3D vector) {
        location.setDirection(TypeUtil.adapt(vector));
        return this;
    }

    @Override
    public void setPitch(float pitch) {
        location.setPitch(pitch);
    }

    @Override
    public void setYaw(float yaw) {
        location.setYaw(yaw);
    }

    @Override
    public void setWorld(World world) {
        WorldWrapper wrapper = (WorldWrapper)world;
        location.setWorld(wrapper.getBukkitWorld());
    }

    @Override
    public void setX(double x) {
        location.setX(x);
    }

    @Override
    public void setY(double y) {
        location.setY(y);
    }

    @Override
    public void setZ(double z) {
        location.setZ(z);
    }

    @Override
    public Location subtract(double x, double y, double z) {
        location = location.subtract(x, y, z);
        return this;
    }

    @Override
    public Location subtract(Location loc) {
        LocationWrapper wrapper = (LocationWrapper)loc;
        location = location.subtract(wrapper.getBukkitLocation());
        return this;
    }

    @Override
    public Location subtract(Vector3D vec) {
        location = location.subtract(TypeUtil.adapt(vec));
        return this;
    }

    @Override
    public Vector3D toVector() {
        return new Vector3D(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public Location zero() {
        location.zero();
        return this;
    }
}
