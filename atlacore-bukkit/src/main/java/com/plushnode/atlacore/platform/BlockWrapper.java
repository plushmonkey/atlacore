package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.TypeUtil;

public class BlockWrapper implements Block {
    private org.bukkit.block.Block block;

    public BlockWrapper(org.bukkit.block.Block block) {
        this.block = block;
    }

    public org.bukkit.block.Block getBukkitBlock() {
        return block;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockWrapper) {
            return block.equals(((BlockWrapper)obj).block);
        }
        return block.equals(obj);
    }

    @Override
    public int hashCode() {
        return block.hashCode();
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
