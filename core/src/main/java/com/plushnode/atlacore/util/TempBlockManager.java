package com.plushnode.atlacore.util;

import com.plushnode.atlacore.Location;

import java.util.HashMap;
import java.util.Map;

public class TempBlockManager {
    private Map<Location, TempBlock> temporaryBlocks;

    public TempBlockManager() {
        this.temporaryBlocks = new HashMap<>();
    }

    public void add(TempBlock block) {
        Location location = block.getPreviousState().getLocation();

        temporaryBlocks.put(location, block);
    }

    public void reset(final Location location) {
        TempBlock block = temporaryBlocks.get(location);

        if (block != null) {
            block.reset();
            temporaryBlocks.remove(location);
        }
    }

    public void resetAll() {
        for (TempBlock block : temporaryBlocks.values()) {
            block.reset();
        }
        temporaryBlocks.clear();
    }

    public TempBlock getTempBlock(Location location) {
        return temporaryBlocks.get(location);
    }

    public boolean isTempBlock(Location location) {
        return temporaryBlocks.containsKey(location);
    }
}
