package com.plushnode.atlacore;

import com.plushnode.atlacore.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

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
    Vector3D getDirection();
    float getPitch();
    float getYaw();
    World getWorld();
    double getX();
    double getY();
    double getZ();
    double length();
    double lengthSquared();
    Location multiply(double m);
    Location setDirection(Vector3D vector);
    void setPitch(float pitch);
    void setYaw(float yaw);
    void setWorld(World world);
    void setX(double x);
    void setY(double y);
    void setZ(double z);
    Location subtract(double x, double y, double z);
    Location subtract(Location loc);
    Location subtract(Vector3D vec);
    String toString();
    Vector3D toVector();
    Location zero();
}
