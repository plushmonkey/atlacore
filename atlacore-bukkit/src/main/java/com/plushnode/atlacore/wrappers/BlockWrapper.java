package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockFace;
import com.plushnode.atlacore.block.BlockState;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.util.TypeUtil;

public class BlockWrapper implements Block {
    private org.bukkit.block.Block block;

    public BlockWrapper(org.bukkit.block.Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return block.toString();
    }

    @Override
    public boolean breakNaturally() {
        return block.breakNaturally();
    }

    @Override
    public int getTypeId() {
        return block.getTypeId();
    }

    @Override
    public byte getData() {
        return block.getData();
    }

    @Override
    public Material getType() {
        return TypeUtil.adapt(block.getType());
    }

    @Override
    public Location getLocation() {
        return new LocationWrapper(block.getLocation());
    }

    @Override
    public Block getRelative(BlockFace face) {
        org.bukkit.block.BlockFace bukkitFace = org.bukkit.block.BlockFace.values()[face.ordinal()];
        return new BlockWrapper(block.getRelative(bukkitFace));
    }

    @Override
    public Block getRelative(BlockFace face, int distance) {
        org.bukkit.block.BlockFace bukkitFace = org.bukkit.block.BlockFace.values()[face.ordinal()];
        return new BlockWrapper(block.getRelative(bukkitFace, distance));
    }

    @Override
    public BlockState getState() {
        return new BlockStateWrapper(block.getState());
    }

    @Override
    public World getWorld() {
        return new WorldWrapper(block.getWorld());
    }

    @Override
    public int getX() {
        return block.getX();
    }

    @Override
    public int getY() {
        return block.getY();
    }

    @Override
    public int getZ() {
        return block.getZ();
    }

    @Override
    public boolean isLiquid() {
        return block.isLiquid();
    }

    @Override
    public void setData(byte data) {
        block.setData(data);
    }

    @Override
    public void setType(Material type) {
        block.setType(TypeUtil.adapt(type));
    }

    @Override
    public void setTypeId(int typeId) {
        block.setTypeId(typeId);
    }

    @Override
    public void setTypeId(int typeId, boolean applyPhysics) {
        block.setTypeId(typeId, applyPhysics);
    }

    @Override
    public void setTypeIdAndData(int typeId, byte data, boolean applyPhysics) {
        block.setTypeIdAndData(typeId, data, applyPhysics);
    }
}
