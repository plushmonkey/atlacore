package com.plushnode.atlacore.platform.data;

import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.spongepowered.api.data.manipulator.mutable.block.FluidLevelData;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Levelled implements com.plushnode.atlacore.platform.block.data.Levelled {
    private int level;
    private Material material;

    public Levelled(Location<World> location, FluidLevelData data) {
        this.level = data.level().get();
        this.material = SpongeTypeUtil.adapt(location.getBlock().getType());
    }

    public Levelled(Material material, int level) {
        this.material = material;
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getMaximumLevel() {
        return 15;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public String getAsString() {
        return "";
    }

    @Override
    public BlockData clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            //
        }

        return new Levelled(material, level);
    }
}
