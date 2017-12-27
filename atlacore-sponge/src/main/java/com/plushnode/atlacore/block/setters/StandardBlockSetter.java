package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.Material;

public class StandardBlockSetter implements BlockSetter {
    @Override
    public void setBlock(Location location, int typeId, byte data) {
        location.getBlock().setTypeId(typeId);
    }

    @Override
    public void setBlock(Location location, Material material) {
        location.getBlock().setType(material);
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
