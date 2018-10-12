package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.util.SpongeMaterialUtil;
import com.plushnode.atlacore.util.SpongeVersionUtil;

public class BlockStateWrapper implements BlockState {
    private org.spongepowered.api.block.BlockState state;
    private org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location;

    public BlockStateWrapper(org.spongepowered.api.block.BlockState state, org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location) {
        this.state = state;
        this.location = location;
    }

    public org.spongepowered.api.block.BlockState getSpongeBlockState() {
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
        return new BlockWrapper(location);
    }

    @Override
    public Location getLocation() {
        return new LocationWrapper(location);
    }

    @Override
    public BlockData getBlockData() {
        // todo
        return null;
    }

    @Override
    public Material getType() {
        return SpongeMaterialUtil.toMaterial(state.getType());
    }

    @Override
    public World getWorld() {
        return new WorldWrapper(location.getExtent());
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @Override
    public void setBlockData(BlockData data) {

    }

    @Override
    public void setType(Material type) {
        SpongeVersionUtil.setBlockType(location, SpongeMaterialUtil.toBlockType(type));

        state = location.getBlock();
    }

    @Override
    public boolean update() {
        return SpongeVersionUtil.setBlockType(location, state.getType());
    }

    @Override
    public boolean update(boolean force) {
        return update();
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return update();
    }
}
