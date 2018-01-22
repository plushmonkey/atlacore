package com.plushnode.atlacore.util;

import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.BlockState;
import com.plushnode.atlacore.block.Material;

public class TempBlock {
    private BlockState previousState;
    private Material tempType;
    private boolean applyPhysics;
    private BlockSetter setter;

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

        Game.getTempBlockManager().add(this);
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

        Game.getTempBlockManager().add(this);
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
        Game.getTempBlockManager().remove(this);

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
}
