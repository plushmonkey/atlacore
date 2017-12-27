package com.plushnode.atlacore.wrappers;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.World;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockFace;
import com.plushnode.atlacore.block.BlockState;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.util.SpongeMaterialUtil;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;

public class BlockWrapper implements Block {
    private org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location;

    public BlockWrapper(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> location) {
        Vector3i bp = location.getBlockPosition();
        // Align block location to grid.
        this.location = location.setPosition(new Vector3d(bp.getX(), bp.getY(), bp.getZ()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockWrapper) {
            return location.equals(((BlockWrapper)obj).location);
        }
        
        return location.equals(obj);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public String toString() {
        return location.toString();
    }

    @Override
    public boolean breakNaturally() {
        // todo: figure out how to actually do this
        return location.removeBlock(Cause.of(NamedCause.of(NamedCause.PLAYER_BREAK, this)));
    }

    @Override
    public int getTypeId() {
        throw new UnsupportedOperationException("Blocks should be using newer api.");
    }

    @Override
    public byte getData() {
        // todo: impl
        return 0;
    }

    @Override
    public Material getType() {
        return SpongeMaterialUtil.toMaterial(location.getBlock().getType());
    }

    @Override
    public Location getLocation() {
        return new LocationWrapper(location);
    }

    @Override
    public Block getRelative(BlockFace face) {
        return new BlockWrapper(location.getBlockRelative(SpongeTypeUtil.adapt(face)));
    }

    @Override
    public Block getRelative(BlockFace face, int distance) {
        Direction dir = SpongeTypeUtil.adapt(face);
        org.spongepowered.api.world.Location<org.spongepowered.api.world.World> blockLoc = location.copy();

        for (int i = 0; i < distance; ++i) {
            blockLoc = blockLoc.getBlockRelative(dir);
        }
        return new BlockWrapper(blockLoc);
    }

    @Override
    public BlockState getState() {
        return new BlockStateWrapper(location.getBlock(), location);
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
    public boolean isLiquid() {
        Material material = SpongeMaterialUtil.toMaterial(location.getBlock().getType());
        return material == Material.WATER ||
               material == Material.STATIONARY_WATER ||
               material == Material.LAVA ||
               material == Material.STATIONARY_LAVA;
    }

    @Override
    public void setData(byte data) {

    }

    @Override
    public void setType(Material type) {
        PluginContainer container = Sponge.getPluginManager().fromInstance(AtlaPlugin.plugin).orElse(null);
        location.setBlockType(SpongeMaterialUtil.toBlockType(type), Cause.source(container).build());
    }

    @Override
    public void setTypeId(int typeId) {
        Material material = SpongeMaterialUtil.fromTypeId(typeId);
        setType(material);
    }

    @Override
    public void setTypeId(int typeId, boolean applyPhysics) {
        // todo: physics
        setTypeId(typeId);
    }

    @Override
    public void setTypeIdAndData(int typeId, byte data, boolean applyPhysics) {
        // todo: data?/physics
        setTypeId(typeId);
    }
}
