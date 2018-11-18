package com.plushnode.atlacore.platform;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.platform.data.Levelled;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.material.SpongeMaterialUtil;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import com.plushnode.atlacore.util.SpongeVersionUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.FluidLevelData;
import org.spongepowered.api.util.Direction;

import java.util.Optional;

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
        return false;
    }

    @Override
    public BlockData getBlockData() {
        Optional<FluidLevelData> fluidLevel = location.get(FluidLevelData.class);

        if (fluidLevel.isPresent()) {
            return new Levelled(location, fluidLevel.get());
        }

        return null;
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
               material == Material.LAVA;
    }

    @Override
    public void setBlockData(BlockData data) {
        if (data instanceof Levelled) {
            Levelled levelled = (Levelled)data;

            // Fluid level seems to not be implemented... ?
            /*System.out.println("block key present: " + location.getBlock().get(Keys.FLUID_LEVEL).isPresent());

            FluidLevelData fluidData =
                    Sponge.getDataManager().getManipulatorBuilder(FluidLevelData.class).get().create();

            fluidData.set(fluidData.level().set(levelled.getLevel()));

            org.spongepowered.api.block.BlockState newState =
                    SpongeMaterialUtil.toBlockType(data.getMaterial()).getDefaultState()
                    .with(fluidData.asImmutable()).get();

            SpongeVersionUtil.setBlock(location, newState);*/
        }
    }

    @Override
    public void setType(Material type) {
        SpongeVersionUtil.setBlockType(location, SpongeMaterialUtil.toBlockType(type));
    }

    @Override
    public void setType(Material type, boolean applyPhysics) {
        SpongeVersionUtil.setBlockType(location, SpongeMaterialUtil.toBlockType(type));
    }

    @Override
    public AABB getBounds() {
        // todo: get actual block bounding box instead of binary solid
        if (MaterialUtil.isTransparent(this)) {
            return new AABB(null, null);
        }

        return new AABB(new Vector3D(0, 0, 0), new Vector3D(1, 1, 1));
    }

    @Override
    public boolean hasBounds() {
        return getBounds().max() != null;
    }
}
