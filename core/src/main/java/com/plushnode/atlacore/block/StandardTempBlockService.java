package com.plushnode.atlacore.block;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.util.Task;

import java.util.*;

public class StandardTempBlockService implements TempBlockService {
    private Map<Location, TempBlock> temporaryBlocks = new HashMap<>();
    private Task task;

    @Override
    public void add(TempBlock block) {
        setBlock(block);

        Location location = block.getPreviousState().getLocation();

        temporaryBlocks.put(location, block);
    }

    @Override
    public void refresh(TempBlock tempBlock) {
        Block block = tempBlock.getBlock();

        if (tempBlock.isApplyPhysics()) {
            block.setType(tempBlock.getTempType());
        } else {
            setBlock(tempBlock);
        }
    }

    private void setBlock(TempBlock tempBlock) {
        if (tempBlock.getBlockData() == null) {
            tempBlock.getSetter().setBlock(tempBlock.getBlock(), tempBlock.getTempType());
        } else {
            tempBlock.getSetter().setBlock(tempBlock.getBlock(), tempBlock.getTempType(), tempBlock.getBlockData());
        }
    }

    // Stops tracking a TempBlock without resetting it.
    @Override
    public void remove(TempBlock block) {
        Location location = block.getPreviousState().getLocation();

        temporaryBlocks.remove(location);
    }

    @Override
    public void reset(final Block block) {
        reset(block.getLocation());
    }

    @Override
    public void reset(final Location location) {
        reset(temporaryBlocks.get(location));
    }

    @Override
    public void reset(final TempBlock tempBlock) {
        if (tempBlock != null) {
            temporaryBlocks.remove(tempBlock.getPreviousState().getLocation());

            if (tempBlock.isApplyPhysics()) {
                tempBlock.getPreviousState().update(true);
            } else {
                tempBlock.getSetter().setBlock(tempBlock.getPreviousState());
            }
        }
    }

    @Override
    public void resetAll() {
        List<TempBlock> temps = new ArrayList<>(temporaryBlocks.values());

        for (TempBlock block : temps) {
            block.reset();
        }
        temporaryBlocks.clear();
    }

    @Override
    public TempBlock getTempBlock(Location location) {
        return temporaryBlocks.get(location);
    }

    @Override
    public TempBlock getTempBlock(Block block) {
        return getTempBlock(block.getLocation());
    }

    @Override
    public boolean isTempBlock(Block block) {
        return temporaryBlocks.containsKey(block.getLocation());
    }

    @Override
    public boolean isTempBlock(Location location) {
        return temporaryBlocks.containsKey(location);
    }

    @Override
    public void start() {
        task = Game.plugin.createTaskTimer(this::update, 1, 1);
    }

    @Override
    public void stop() {
        resetAll();

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
