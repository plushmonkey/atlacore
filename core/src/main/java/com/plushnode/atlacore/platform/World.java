package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface World {
    Block getBlockAt(int x, int y, int z);
    Block getBlockAt(Location location);

    Location getLocation(double x, double y, double z);
    default Location getLocation(Vector3D position) {
        return getLocation(position.getX(), position.getY(), position.getZ());
    }

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
