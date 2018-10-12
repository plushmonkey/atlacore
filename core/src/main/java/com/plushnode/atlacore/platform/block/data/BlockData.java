package com.plushnode.atlacore.platform.block.data;

import com.plushnode.atlacore.platform.block.Material;

public interface BlockData extends Cloneable {
    Material getMaterial();
    String getAsString();
    BlockData clone();
}
