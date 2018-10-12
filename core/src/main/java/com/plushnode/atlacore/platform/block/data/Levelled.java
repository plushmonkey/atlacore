package com.plushnode.atlacore.platform.block.data;

public interface Levelled extends BlockData {
    int getLevel();
    void setLevel(int level);
    int getMaximumLevel();
}
