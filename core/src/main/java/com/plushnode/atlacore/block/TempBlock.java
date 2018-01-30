package com.plushnode.atlacore.block;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;

public class TempBlock {
    private BlockState previousState;
    private Material tempType;
    private boolean applyPhysics;
    private BlockSetter setter;
    private long endTime;

    public TempBlock(Block block, Material tempType) {
        this(block, tempType, false);
    }

    public TempBlock(BlockState blockState, Material tempType) {
        this(blockState, tempType, false);
    }

    public TempBlock(Block block, Material tempType, boolean applyPhysics) {
        this.previousState = block.getState();
        this.tempType = tempType;
        this.applyPhysics = applyPhysics;

        BlockSetter.Flag flag = BlockSetter.Flag.FAST;
        if (applyPhysics) {
            flag = BlockSetter.Flag.LIGHTING;
        }

        setter = Game.plugin.getBlockSetter(flag);
        setter.setBlock(block, tempType);

        Game.getTempBlockService().add(this);
    }

    public TempBlock(BlockState blockState, Material tempType, boolean applyPhysics) {
        this.previousState = blockState;
        this.tempType = tempType;
        this.applyPhysics = false;

        BlockSetter.Flag flag = BlockSetter.Flag.FAST;
        if (applyPhysics) {
            flag = BlockSetter.Flag.LIGHTING;
        }

        setter = Game.plugin.getBlockSetter(flag);
        setter.setBlock(previousState.getBlock(), tempType);

        Game.getTempBlockService().add(this);
    }

    public TempBlock(Block block, Material tempType, long duration) {
        this(block, tempType, false);
        endTime = System.currentTimeMillis() + duration;
    }

    public TempBlock(BlockState blockState, Material tempType, long duration) {
        this(blockState, tempType, false);
        endTime = System.currentTimeMillis() + duration;
    }

    public TempBlock(Block block, Material tempType, boolean applyPhysics, long duration) {
        this(block, tempType, applyPhysics);
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
            setter.setBlock(block, tempType);
        }
    }

    public void reset() {
        Game.getTempBlockService().remove(this);

        if (this.applyPhysics) {
            this.previousState.update(true);
        } else {
            Material previousType = this.previousState.getType();
            setter.setBlock(previousState.getBlock(), previousType);
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
