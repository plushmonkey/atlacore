package com.plushnode.atlacore.block;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;

public class TempBlock {
    private BlockState previousState;
    private Material tempType;
    private BlockData data;
    private boolean applyPhysics;
    private BlockSetter setter;
    private long endTime;

    public TempBlock(Block block, Material tempType) {
        this(block, tempType, null, false);
    }

    public TempBlock(Block block, BlockData data) {
        this(block, data.getMaterial(), data, false);
    }

    public TempBlock(Block block, Material tempType, boolean applyPhysics) {
        this(block, tempType, null, applyPhysics);
    }

    public TempBlock(Block block, Material tempType, BlockData data, boolean applyPhysics) {
        this.previousState = block.getState();
        this.tempType = tempType;
        this.data = data;
        this.applyPhysics = applyPhysics;

        BlockSetter.Flag flag = BlockSetter.Flag.FAST;
        if (applyPhysics || tempType == Material.FIRE) {
            flag = BlockSetter.Flag.LIGHTING;
        }

        TempBlock previous = Game.getTempBlockService().getTempBlock(block);
        if (previous != null) {
            previous.reset();
            this.previousState = previous.previousState;
        }

        setter = Game.plugin.getBlockSetter(flag);

        if (data == null) {
            setter.setBlock(block, tempType);
        } else {
            setter.setBlock(block, tempType, data);
        }

        Game.getTempBlockService().add(this);
    }

    public TempBlock(BlockState blockState, Material tempType) {
        this(blockState, tempType, false);
    }

    public TempBlock(BlockState blockState, Material tempType, boolean applyPhysics) {
        this.previousState = blockState;
        this.tempType = tempType;
        this.applyPhysics = false;

        BlockSetter.Flag flag = BlockSetter.Flag.FAST;
        if (applyPhysics || tempType == Material.FIRE) {
            flag = BlockSetter.Flag.LIGHTING;
        }

        TempBlock previous = Game.getTempBlockService().getTempBlock(blockState.getLocation());
        if (previous != null) {
            previous.reset();
            this.previousState = previous.previousState;
        }

        setter = Game.plugin.getBlockSetter(flag);
        setter.setBlock(previousState.getBlock(), tempType);

        Game.getTempBlockService().add(this);
    }

    public TempBlock(Block block, Material tempType, long duration) {
        this(block, tempType, null, false);
        endTime = System.currentTimeMillis() + duration;
    }

    public TempBlock(BlockState blockState, Material tempType, long duration) {
        this(blockState, tempType, false);
        endTime = System.currentTimeMillis() + duration;
    }

    public TempBlock(Block block, Material tempType, boolean applyPhysics, long duration) {
        this(block, tempType, null, applyPhysics);
        endTime = System.currentTimeMillis() + duration;
    }

    public TempBlock(BlockState blockState, Material tempType, boolean applyPhysics, long duration) {
        this(blockState, tempType, applyPhysics);
        endTime = System.currentTimeMillis() + duration;
    }

    // Refresh the block to the temporary state
    public void update() {
        Block block = previousState.getBlock();
        if (this.applyPhysics) {
            block.setType(tempType);
        } else {
            if (data == null) {
                setter.setBlock(block, tempType);
            } else {
                setter.setBlock(block, tempType, data);
            }
        }
    }

    public void reset() {
        Game.getTempBlockService().remove(this);

        if (this.applyPhysics) {
            this.previousState.update(true);
        } else {
            setter.setBlock(previousState);
        }
    }

    public Material getTempType() {
        return tempType;
    }

    public BlockState getPreviousState() {
        return previousState;
    }

    public long getEndTime() {
        return this.endTime;
    }
}
