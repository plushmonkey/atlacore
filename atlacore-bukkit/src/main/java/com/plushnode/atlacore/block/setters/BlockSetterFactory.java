package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.platform.block.BlockSetter;

public class BlockSetterFactory {
    private BlockSetter normalBlockSetter;
    private BlockSetter fastBlockSetter;

    public BlockSetterFactory() {
        normalBlockSetter = new StandardBlockSetter();
        fastBlockSetter = new FastBlockSetter();
    }

    public BlockSetter getBlockSetter(BlockSetter.Flag... flags) {
        if (!NativeMethods.enabled) return normalBlockSetter;

        for (BlockSetter.Flag flag : flags) {
            if (flag == BlockSetter.Flag.LIGHTING) {
                return normalBlockSetter;
            }
        }

        return fastBlockSetter;
    }
}
