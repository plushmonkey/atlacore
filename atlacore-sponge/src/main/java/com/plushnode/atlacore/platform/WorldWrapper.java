package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.entity.EntityFactory;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.PassableProperty;

import java.util.*;
import java.util.stream.Collectors;

public class WorldWrapper implements World {
    private org.spongepowered.api.world.World world;

    public WorldWrapper(org.spongepowered.api.world.World world) {
        this.world = world;
    }

    public org.spongepowered.api.world.World getSpongeWorld() {
        return this.world;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorldWrapper) {
            return world.equals(((WorldWrapper)obj).world);
        }
        return world.equals(obj);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public String toString() {
        return world.toString();
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return new BlockWrapper(world.getLocation(x, y, z));
    }

    @Override
    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public Location getLocation(double x, double y, double z) {
        return new LocationWrapper(new org.spongepowered.api.world.Location<>(world, x, y, z));
    }

    @Override
    public List<Entity> getEntities() {
        return this.world.getEntities().stream().map(EntityFactory::createEntity).collect(Collectors.toList());
    }

    @Override
    public long getFullTime() {
        return world.getProperties().getTotalTime();
    }

    @Override
    public Block getHighestBlockAt(int x, int z) {
        int y;
        for (y = 255; y >= 0; --y) {
            BlockState state = world.getBlock(x, y, z);

            Optional<PassableProperty> result = state.getProperty(PassableProperty.class);
            if (result.isPresent()) {
                PassableProperty prop = result.get();
                if (prop.getValue() != null && prop.getValue()) {
                    continue;
                }
            }
            break;
        }

        return getBlockAt(x, y, z);
    }

    @Override
    public Block getHighestBlockAt(Location location) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public double getHumidity(int x, int z) {
        return 0.0;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        return world.getEntities().stream()
                .filter(e -> e.supports(Keys.HEALTH))
                .map(LivingEntityWrapper::new)
                .collect(Collectors.toList());
    }

    @Override
    public int getMaxHeight() {
        // todo: find out how to actually do this.
        return 255;
    }

    @Override
    public String getName() {
        return world.getName();
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        double radius = Math.max(x, Math.max(y, z));

        return world.getEntities(e -> SpongeTypeUtil.adapt(e.getLocation()).distanceSquared(location) <= radius).stream()
                .map(EntityFactory::createEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getSeaLevel() {
        // todo: find out how to actually do this.
        return 64;
    }

    @Override
    public double getTemperature(int x, int z) {
        // todo: find out how to actually do this.
        return 0.0;
    }

    @Override
    public long getTime() {
        return world.getProperties().getWorldTime();
    }

    @Override
    public UUID getUID() {
        return world.getUniqueId();
    }
}
