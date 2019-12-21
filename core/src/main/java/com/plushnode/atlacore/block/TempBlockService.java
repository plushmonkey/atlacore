package com.plushnode.atlacore.block;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;

public interface TempBlockService  {
    void add(TempBlock block);

    void refresh(TempBlock tempBlock);

    // Stops tracking a TempBlock without resetting it.
    void remove(TempBlock block);

    void reset(final Block block);
    void reset(final Location location);
    void reset(final TempBlock tempBlock);

    void resetAll();

    TempBlock getTempBlock(Location location);
    TempBlock getTempBlock(Block block);

    boolean isTempBlock(Block block);
    boolean isTempBlock(Location location);

    void start();
    void stop();

    int size();
}
