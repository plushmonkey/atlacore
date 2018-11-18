package com.plushnode.atlacore.platform.data;

import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.util.TypeUtil;
import org.bukkit.block.data.Levelled;

public final class BlockDataFactory {
    public static BlockData createBlockData(Material material) {
        org.bukkit.block.data.BlockData data = TypeUtil.adapt(material).createBlockData();

        if (data instanceof Levelled) {
            return new LevelledWrapper((Levelled)data);
        }

        return null;
    }
}
