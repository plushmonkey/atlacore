package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.block.BlockSetter;

public class BlockSetterFactory {
    private BlockSetter normalBlockSetter;

    public BlockSetterFactory() {
        normalBlockSetter = new StandardBlockSetter();
    }

    public BlockSetter getBlockSetter(BlockSetter.Flag... flags) {
        return normalBlockSetter;
    }
}
