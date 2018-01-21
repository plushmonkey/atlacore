package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.Entity;
import com.plushnode.atlacore.LivingEntity;
import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.entity.EntityFactory;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WorldWrapper implements com.plushnode.atlacore.World {
    private World world;

    public WorldWrapper(World world) {
        this.world = world;
    }

    public World getBukkitWorld() {
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
        return new BlockWrapper(world.getBlockAt(x, y, z));
    }

    @Override
    public Block getBlockAt(Location location) {
        LocationWrapper wrapper = (LocationWrapper)location;
        return new BlockWrapper(world.getBlockAt(wrapper.getBukkitLocation()));
    }

    @Override
    public List<Entity> getEntities() {
        return this.world.getEntities().stream().map(EntityFactory::createEntity).collect(Collectors.toList());
    }

    @Override
    public long getFullTime() {
        return this.world.getFullTime();
    }

    @Override
    public Block getHighestBlockAt(int x, int z) {
        return new BlockWrapper(world.getHighestBlockAt(x, z));
    }

    @Override
    public Block getHighestBlockAt(Location location) {
        LocationWrapper wrapper = (LocationWrapper)location;
        return new BlockWrapper(world.getHighestBlockAt(wrapper.getBukkitLocation()));
    }

    @Override
    public double getHumidity(int x, int z) {
        return world.getHumidity(x, z);
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        return this.world.getLivingEntities().stream().map(LivingEntityWrapper::new).collect(Collectors.toList());
    }

    @Override
    public int getMaxHeight() {
        return world.getMaxHeight();
    }

    @Override
    public String getName() {
        return world.getName();
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        LocationWrapper lw = (LocationWrapper)location;

        return world.getNearbyEntities(lw.getBukkitLocation(), x, y, z).stream()
                .map(EntityFactory::createEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public double getTemperature(int x, int z) {
        return world.getTemperature(x, z);
    }

    @Override
    public long getTime() {
        return world.getTime();
    }

    @Override
    public UUID getUID() {
        return world.getUID();
    }
}
