package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.platform.BlockWrapper;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.platform.LocationWrapper;

public class FastBlockSetter implements BlockSetter {
    @Override
    public void setBlock(Location location, Material material) {
        LocationWrapper wrapper = (LocationWrapper)location;
        org.bukkit.Material bukkitMaterial = TypeUtil.adapt(material);

        NativeMethods.setBlockFast(wrapper.getBukkitLocation().getBlock(), bukkitMaterial.getId(), 0);
    }

    @Override
    public void setBlock(Block block, Material material) {
        setBlock(block.getLocation(), material);
    }

    @Override
    public void setBlock(Block block, Material material, BlockData data) {
        // TODO: pass BlockData
        NativeMethods.setBlockFast(((BlockWrapper)block).getBukkitBlock(), material.getId(), (byte)0);
    }
}
