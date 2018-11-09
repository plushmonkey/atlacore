package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.util.TypeUtil;

public class BlockStateWrapper implements BlockState {
    private org.bukkit.block.BlockState state;

    public BlockStateWrapper(org.bukkit.block.BlockState state) {
        this.state = state;
    }

    public org.bukkit.block.BlockState getBukkitState() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockStateWrapper) {
            return state.equals(((BlockStateWrapper)obj).state);
        }
        return state.equals(obj);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public String toString() {
        return state.toString();
    }

    @Override
    public Block getBlock() {
        return new BlockWrapper(state.getBlock());
    }

    @Override
    public Location getLocation() {
        return new LocationWrapper(state.getLocation());
    }

    @Override
    public BlockData getBlockData() {
        // TODO: implement BlockData
        return null;
    }

    @Override
    public Material getType() {
        return TypeUtil.adapt(state.getType());
    }

    @Override
    public World getWorld() {
        return new WorldWrapper(state.getWorld());
    }

    @Override
    public int getX() {
        return state.getX();
    }

    @Override
    public int getY() {
        return state.getY();
    }

    @Override
    public int getZ() {
        return state.getZ();
    }

    @Override
    public void setBlockData(BlockData data) {
        // TODO: Transform block data to bukkit BlockData.
        //state.setBlockData(data);
    }

    @Override
    public void setType(Material type) {
        state.setType(TypeUtil.adapt(type));
    }

    @Override
    public boolean update() {
        return state.update();
    }

    @Override
    public boolean update(boolean force) {
        return state.update(force);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return state.update(force, applyPhysics);
    }
}
