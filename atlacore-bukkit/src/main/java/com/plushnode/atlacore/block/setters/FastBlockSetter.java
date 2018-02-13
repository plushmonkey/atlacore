package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.platform.LocationWrapper;

public class FastBlockSetter implements BlockSetter {
    @Override
    public void setBlock(Location location, int typeId, byte data) {
        LocationWrapper wrapper = (LocationWrapper)location;

        NativeMethods.setBlockFast(wrapper.getBukkitLocation().getBlock(), typeId, data);
    }

    @Override
    public void setBlock(Location location, Material material) {
        LocationWrapper wrapper = (LocationWrapper)location;
        org.bukkit.Material bukkitMaterial = TypeUtil.adapt(material);

        NativeMethods.setBlockFast(wrapper.getBukkitLocation().getBlock(), bukkitMaterial.getId(), 0);
    }

    @Override
    public void setBlock(Block block, int typeId, byte data) {
        setBlock(block.getLocation(), typeId, data);
    }

    @Override
    public void setBlock(Block block, Material material) {
        setBlock(block.getLocation(), material);
    }

    @Override
    public void setBlock(Block block, Material material, byte data) {
        setBlock(block.getLocation(), material.getId(), data);
    }
}
