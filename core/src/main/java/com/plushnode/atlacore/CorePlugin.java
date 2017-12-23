package com.plushnode.atlacore;

import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.config.Configuration;

public interface CorePlugin {
    void onConfigReload(Configuration config);

    BlockSetter getBlockSetter();
    BlockSetter getBlockSetter(BlockSetter.Flag... flags);
}
