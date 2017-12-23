package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.LocationWrapper;

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
}
