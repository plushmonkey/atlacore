package com.plushnode.atlacore.platform.data;

import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.platform.block.data.Levelled;
import com.plushnode.atlacore.util.TypeUtil;

public class LevelledWrapper implements Levelled {
    private org.bukkit.block.data.Levelled levelled;

    public LevelledWrapper(org.bukkit.block.data.Levelled levelled) {
        this.levelled = levelled;
    }

    public org.bukkit.block.data.Levelled getLevelled() {
        return this.levelled;
    }

    @Override
    public int getLevel() {
        return levelled.getLevel();
    }

    @Override
    public void setLevel(int level) {
        levelled.setLevel(level);
    }

    @Override
    public int getMaximumLevel() {
        return levelled.getMaximumLevel();
    }

    @Override
    public Material getMaterial() {
        return TypeUtil.adapt(levelled.getMaterial());
    }

    @Override
    public String getAsString() {
        return levelled.getAsString();
    }

    @Override
    public BlockData clone() {
        try {
            Object clone = super.clone();
            return new LevelledWrapper((org.bukkit.block.data.Levelled) clone);
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
