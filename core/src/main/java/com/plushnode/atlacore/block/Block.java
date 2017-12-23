package com.plushnode.atlacore.block;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;

public interface Block {
    boolean breakNaturally();

    int getTypeId();
    byte getData();
    Material getType();
    Location getLocation();

    Block getRelative(BlockFace face);
    Block getRelative(BlockFace face, int distance);

    BlockState getState();
    World getWorld();
    int getX();
    int getY();
    int getZ();
    boolean isLiquid();

    void setData(byte data);
    void setType(Material type);
    void setTypeId(int typeId);
    void setTypeId(int typeId, boolean applyPhysics);
    void setTypeIdAndData(int typeId, byte data, boolean applyPhysics);
}
