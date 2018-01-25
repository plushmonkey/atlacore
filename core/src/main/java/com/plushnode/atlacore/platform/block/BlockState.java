package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.World;

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
