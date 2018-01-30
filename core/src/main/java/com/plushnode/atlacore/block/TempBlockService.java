package com.plushnode.atlacore.block;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.util.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TempBlockService {
    private Map<Location, TempBlock> temporaryBlocks = new HashMap<>();
    private Task task;

    public void add(TempBlock block) {
        Location location = block.getPreviousState().getLocation();

        temporaryBlocks.put(location, block);
    }

    // Stops tracking a TempBlock without resetting it.
    public void remove(TempBlock block) {
        Location location = block.getPreviousState().getLocation();

        temporaryBlocks.remove(location);
    }

    public void reset(final Block block) {
        reset(block.getLocation());
    }

    public void reset(final Location location) {
        TempBlock block = temporaryBlocks.get(location);

        if (block != null) {
            block.reset();
            temporaryBlocks.remove(location);
        }
    }

    public void reset(final TempBlock tempBlock) {
        if (tempBlock != null) {
            tempBlock.reset();
            temporaryBlocks.remove(tempBlock.getPreviousState().getLocation());
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

    public TempBlock getTempBlock(Block block) {
        return getTempBlock(block.getLocation());
    }

    public boolean isTempBlock(Location location) {
        return temporaryBlocks.containsKey(location);
    }

    public void start() {
        task = Game.plugin.createTaskTimer(this::update, 20, 20);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void update() {
        // TODO: This might end up being too slow to iterate through every block so often.
        // TODO: Some kind of priority queue could work.

        long time = System.currentTimeMillis();
        for (Iterator<Map.Entry<Location, TempBlock>> iterator = temporaryBlocks.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<Location, TempBlock> entry = iterator.next();
            long endTime = entry.getValue().getEndTime();

            if (endTime == 0) continue;

            if (time >= endTime) {
                iterator.remove();
                // Reset after removing it from the list so it doesn't try to modify list while iterating.
                entry.getValue().reset();
            }
        }
    }
}
