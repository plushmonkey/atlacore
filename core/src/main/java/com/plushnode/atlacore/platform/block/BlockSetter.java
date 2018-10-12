package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.block.data.BlockData;

public interface BlockSetter {
    void setBlock(Location location, Material material);
    void setBlock(Block block, Material material);
    void setBlock(Block block, Material material, BlockData data);

    enum Flag {
        FAST,
        LIGHTING
    }
}
