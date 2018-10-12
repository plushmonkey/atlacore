package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.World;
import com.plushnode.atlacore.platform.block.data.BlockData;

public interface BlockState {
    Block getBlock();
    Location getLocation();

    BlockData getBlockData();
    Material getType();

    World getWorld();

    int getX();
    int getY();
    int getZ();

    void setBlockData(BlockData data);
    void setType(Material type);

    boolean update();
    boolean update(boolean force);
    boolean update(boolean force, boolean applyPhysics);
}
