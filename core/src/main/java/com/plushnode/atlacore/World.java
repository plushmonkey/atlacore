package com.plushnode.atlacore;

import com.plushnode.atlacore.block.Block;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface World {
    Block getBlockAt(int x, int y, int z);
    Block getBlockAt(Location location);

    List<Entity> getEntities();
    long getFullTime();

    Block getHighestBlockAt(int x, int z);
    Block getHighestBlockAt(Location location);
    double getHumidity(int x, int z);
    List<LivingEntity> getLivingEntities();
    int getMaxHeight();
    String getName();
    Collection<Entity> getNearbyEntities(Location location, double x, double y, double z);

    //List<Player> getPlayers();

    int getSeaLevel();
    double getTemperature(int x, int z);
    long getTime();
    UUID getUID();

    // void playSound(Location location, Sound sound, float volume, float pitch)
}
