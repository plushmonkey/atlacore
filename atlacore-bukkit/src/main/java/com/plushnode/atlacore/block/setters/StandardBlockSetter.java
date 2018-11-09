package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.platform.LocationWrapper;

public class StandardBlockSetter implements BlockSetter {
    @Override
    public void setBlock(Location location, Material material) {
        LocationWrapper wrapper = (LocationWrapper)location;
        wrapper.getBukkitLocation().getBlock().setType(TypeUtil.adapt(material));
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

    @Override
    public void setBlock(BlockState state) {
        state.update();
    }
}
