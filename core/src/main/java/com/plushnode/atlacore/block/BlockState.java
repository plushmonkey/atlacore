package com.plushnode.atlacore.block;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;

public interface BlockState {
    Block getBlock();
    Location getLocation();

    byte getRawData();
    Material getType();
    int getTypeId();

    World getWorld();

    int getX();
    int getY();
    int getZ();

    void setRawData(byte data);
    void setType(Material type);
    void setTypeId(int typeId);

    boolean update();
    boolean update(boolean force);
    boolean update(boolean force, boolean applyPhysics);
}
