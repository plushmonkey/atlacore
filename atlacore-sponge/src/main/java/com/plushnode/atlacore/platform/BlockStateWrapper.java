package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.SpongeMaterialUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;

public class BlockStateWrapper implements BlockState {
    private org.spongepowered.api.block.BlockState state;
    private org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location;

    public BlockStateWrapper(org.spongepowered.api.block.BlockState state, org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location) {
        this.state = state;
        this.location = location;
    }

    public org.spongepowered.api.block.BlockState getSpongeBlockState() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockStateWrapper) {
            return state.equals(((BlockStateWrapper)obj).state);
        }
        return state.equals(obj);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public String toString() {
        return state.toString();
    }

    @Override
    public Block getBlock() {
        return new BlockWrapper(location);
    }

    @Override
    public Location getLocation() {
        return new LocationWrapper(location);
    }

    @Override
    public byte getRawData() {
        // todo
        return 0;
    }

    @Override
    public Material getType() {
        return SpongeMaterialUtil.toMaterial(state.getType());
    }

    @Override
    public int getTypeId() {
        return getType().getId();
    }

    @Override
    public World getWorld() {
        return new WorldWrapper(location.getExtent());
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @Override
    public void setRawData(byte data) {

    }

    @Override
    public void setType(Material type) {
        PluginContainer container = Sponge.getPluginManager().fromInstance(AtlaPlugin.plugin).orElse(null);
        location.setBlockType(SpongeMaterialUtil.toBlockType(type), Cause.source(container).build());
        state = location.getBlock();
    }

    @Override
    public void setTypeId(int typeId) {
        Material type = SpongeMaterialUtil.fromTypeId(typeId);

        PluginContainer container = Sponge.getPluginManager().fromInstance(AtlaPlugin.plugin).orElse(null);
        location.setBlockType(SpongeMaterialUtil.toBlockType(type), Cause.source(container).build());

        state = location.getBlock();
    }

    @Override
    public boolean update() {
        PluginContainer container = Sponge.getPluginManager().fromInstance(AtlaPlugin.plugin).orElse(null);
        return location.setBlockType(state.getType(), Cause.source(container).build());
    }

    @Override
    public boolean update(boolean force) {
        return update();
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return update();
    }
}
