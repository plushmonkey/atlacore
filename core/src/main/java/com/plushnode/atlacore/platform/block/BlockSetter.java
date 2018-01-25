package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.platform.Location;

public interface BlockSetter {
    void setBlock(Location location, int typeId, byte data);
    void setBlock(Location location, Material material);
    void setBlock(Block block, int typeId, byte data);
    void setBlock(Block block, Material material);

    enum Flag {
        FAST,
        LIGHTING
    }
}
