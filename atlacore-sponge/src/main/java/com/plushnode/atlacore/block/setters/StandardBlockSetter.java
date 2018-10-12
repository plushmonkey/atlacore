package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;

public class StandardBlockSetter implements BlockSetter {
    @Override
    public void setBlock(Location location, Material material) {
        location.getBlock().setType(material);
    }

    @Override
    public void setBlock(Block block, Material material) {
        setBlock(block.getLocation(), material);
    }

    @Override
    public void setBlock(Block block, Material material, BlockData data) {
        block.setType(material);

        if (data != null) {
            block.setBlockData(data.clone());
        }
    }
}
