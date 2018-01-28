package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.collision.geometry.AABBProvider;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.World;

public interface Block extends AABBProvider {
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
