package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.collision.geometry.AABBProvider;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.World;
import com.plushnode.atlacore.platform.block.data.BlockData;

public interface Block extends AABBProvider {
    boolean breakNaturally();

    BlockData getBlockData();
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

    void setBlockData(BlockData data);
    void setType(Material type);
    void setType(Material type, boolean applyPhysics);
}
