package com.plushnode.atlacore;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;
import java.util.UUID;

public interface Entity {
    boolean addPassenger(Entity passenger);
    boolean eject();
    int getEntityId();
    float getFallDistance();
    int getFireTicks();
    double getHeight();
    Location getLocation();
    int getMaxFireTicks();
    List<Entity> getNearbyEntities(double x, double y, double z);
    List<Entity> getPassengers();
    UUID getUniqueId();
    Entity getVehicle();
    Vector3D getVelocity();
    double getWidth();
    World getWorld();
    boolean hasGravity();
    boolean isDead();
    boolean isEmpty();
    boolean isGlowing();
    boolean isInsideVehicle();
    boolean isInvulnerable();
    boolean isOnGround();
    boolean isSilent();
    boolean isValid();
    boolean leaveVehicle();
    void remove();
    boolean removePassenger(Entity passenger);
    void setFallDistance(float distance);
    void setFireTicks(int ticks);
    void setGlowing(boolean glowing);
    void setGravity(boolean gravity);
    void setInvulnerable(boolean flag);
    void setSilent(boolean flag);
    void setTickLived(int value);
    void setVelocity(Vector3D velocity);
    boolean teleport(Entity destination);
    boolean teleport(Location location);
}
