package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.LocationWrapper;

public class StandardBlockSetter implements BlockSetter {
    @Override
    public void setBlock(Location location, int typeId, byte data) {
        LocationWrapper wrapper = (LocationWrapper)location;
        wrapper.getBukkitLocation().getBlock().setTypeIdAndData(typeId, data, true);
    }

    @Override
    public void setBlock(Location location, Material material) {
        LocationWrapper wrapper = (LocationWrapper)location;
        wrapper.getBukkitLocation().getBlock().setType(TypeUtil.adapt(material));
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
