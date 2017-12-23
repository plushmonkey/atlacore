package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockState;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.util.TypeUtil;

public class BlockStateWrapper implements BlockState {
    private org.bukkit.block.BlockState state;

    public BlockStateWrapper(org.bukkit.block.BlockState state) {
        this.state = state;
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
    public byte getRawData() {
        return state.getRawData();
    }

    @Override
    public Material getType() {
        return TypeUtil.adapt(state.getType());
    }

    @Override
    public int getTypeId() {
        return state.getTypeId();
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
    public void setRawData(byte data) {
        state.setRawData(data);
    }

    @Override
    public void setType(Material type) {
        state.setType(TypeUtil.adapt(type));
    }

    @Override
    public void setTypeId(int typeId) {
        state.setTypeId(typeId);
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
