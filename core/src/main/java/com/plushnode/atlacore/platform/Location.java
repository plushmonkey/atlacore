package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

// Immutable location
public interface Location {
    Location add(double x, double y, double z);
    Location add(Location vec);
    Location add(Vector3D vec);
    Location clone();
    double distance(Location o);
    double distanceSquared(Location o);
    Block getBlock();
    int getBlockX();
    int getBlockY();
    int getBlockZ();
    World getWorld();
    double getX();
    double getY();
    double getZ();
    double length();
    double lengthSquared();
    Location setWorld(World world);
    Location setX(double x);
    Location setY(double y);
    Location setZ(double z);
    Location subtract(double x, double y, double z);
    Location subtract(Location loc);
    Location subtract(Vector3D vec);
    String toString();
    Vector3D toVector();
}
