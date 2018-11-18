package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.BukkitAABB;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.platform.data.LevelledWrapper;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.TypeUtil;
import org.bukkit.block.data.Levelled;

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
    public BlockData getBlockData() {
        org.bukkit.block.data.BlockData bukkitData = block.getBlockData();

        if (bukkitData instanceof Levelled) {
            return new LevelledWrapper((Levelled)bukkitData);
        }

        return null;
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
    public void setBlockData(BlockData data) {
        if (data instanceof LevelledWrapper) {
            block.setBlockData(((LevelledWrapper) data).getLevelled());
        }
    }

    @Override
    public void setType(Material type) {
        block.setType(TypeUtil.adapt(type));
    }

    @Override
    public void setType(Material type, boolean applyPhysics) {
        block.setType(TypeUtil.adapt(type), applyPhysics);
    }

    @Override
    public AABB getBounds() {
        if (MaterialUtil.isTransparent(this)) {
            return new AABB(null, null, this.getWorld());
        }
        return BukkitAABB.getBlockBounds(block);
    }

    @Override
    public boolean hasBounds() {
        return getBounds().max() != null;
    }
}
