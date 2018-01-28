package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.BukkitAABB;
import com.plushnode.atlacore.util.TypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityWrapper implements Entity {
    protected org.bukkit.entity.Entity entity;

    public EntityWrapper(org.bukkit.entity.Entity entity) {
        this.entity = entity;
    }

    public org.bukkit.entity.Entity getBukkitEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityWrapper) {
            return entity.equals(((EntityWrapper)obj).entity);
        }
        return entity.equals(obj);
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }

    @Override
    public String toString() {
        return entity.toString();
    }

    @Override
    public boolean addPassenger(Entity passenger) {
        EntityWrapper wrapper = (EntityWrapper)passenger;
        return entity.addPassenger(wrapper.getBukkitEntity());
    }

    @Override
    public boolean eject() {
        return entity.eject();
    }

    @Override
    public float getFallDistance() {
        return entity.getFallDistance();
    }

    @Override
    public int getFireTicks() {
        return entity.getFireTicks();
    }

    @Override
    public double getHeight() {
        return entity.getHeight();
    }

    @Override
    public Location getLocation() {
        return new LocationWrapper(entity.getLocation());
    }

    @Override
    public int getMaxFireTicks() {
        return entity.getMaxFireTicks();
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        List<org.bukkit.entity.Entity> entities = entity.getNearbyEntities(x, y, z);
        return entities.stream().map(EntityWrapper::new).collect(Collectors.toList());
    }

    @Override
    public List<Entity> getPassengers() {
        List<org.bukkit.entity.Entity> entities = entity.getPassengers();
        return entities.stream().map(EntityWrapper::new).collect(Collectors.toList());
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    @Override
    public Entity getVehicle() {
        return new EntityWrapper(entity.getVehicle());
    }

    @Override
    public Vector3D getVelocity() {
        Vector v = entity.getVelocity();
        return new Vector3D(v.getX(), v.getY(), v.getZ());
    }

    @Override
    public double getWidth() {
        return entity.getWidth();
    }

    @Override
    public World getWorld() {
        return new WorldWrapper(entity.getWorld());
    }

    @Override
    public boolean hasGravity() {
        return entity.hasGravity();
    }

    @Override
    public boolean isDead() {
        return entity.isDead();
    }

    @Override
    public boolean isEmpty() {
        return entity.isEmpty();
    }

    @Override
    public boolean isGlowing() {
        return entity.isGlowing();
    }

    @Override
    public boolean isInsideVehicle() {
        return entity.isInsideVehicle();
    }

    @Override
    public boolean isInvulnerable() {
        return entity.isInvulnerable();
    }

    @Override
    public boolean isOnGround() {
        return entity.isOnGround();
    }

    @Override
    public boolean isSilent() {
        return entity.isSilent();
    }

    @Override
    public boolean isValid() {
        return entity.isValid();
    }

    @Override
    public boolean leaveVehicle() {
        return entity.leaveVehicle();
    }

    @Override
    public void remove() {
        entity.remove();
    }

    @Override
    public boolean removePassenger(Entity passenger) {
        EntityWrapper wrapper = (EntityWrapper)passenger;
        return entity.removePassenger(wrapper.getBukkitEntity());
    }

    @Override
    public void setFallDistance(float distance) {
        entity.setFallDistance(distance);
    }

    @Override
    public void setFireTicks(int ticks) {
        entity.setFireTicks(ticks);
    }

    @Override
    public void setGlowing(boolean glowing) {
        entity.setGlowing(glowing);
    }

    @Override
    public void setGravity(boolean gravity) {
        entity.setGravity(gravity);
    }

    @Override
    public void setInvulnerable(boolean flag) {
        entity.setInvulnerable(flag);
    }

    @Override
    public void setSilent(boolean flag) {
        entity.setSilent(flag);
    }

    @Override
    public void setTickLived(int value) {
        entity.setTicksLived(value);
    }

    @Override
    public void setVelocity(Vector3D velocity) {
        entity.setVelocity(TypeUtil.adapt(velocity));
    }

    @Override
    public boolean teleport(Entity destination) {
        EntityWrapper wrapper = (EntityWrapper)destination;
        return entity.teleport(wrapper.getBukkitEntity());
    }

    @Override
    public boolean teleport(Location location) {
        LocationWrapper wrapper = (LocationWrapper)location;
        return entity.teleport(wrapper.getBukkitLocation());
    }

    @Override
    public Vector3D getDirection() {
        return TypeUtil.adapt(TypeUtil.adapt(getLocation()).getDirection());
    }

    @Override
    public void setDirection(Vector3D direction) {
        entity.getLocation().setDirection(TypeUtil.adapt(direction));
    }

    @Override
    public float getPitch() {
        return entity.getLocation().getPitch();
    }

    @Override
    public float getYaw() {
        return entity.getLocation().getYaw();
    }

    @Override
    public void setPitch(float pitch) {
        entity.getLocation().setPitch(pitch);
    }

    @Override
    public void setYaw(float yaw) {
        entity.getLocation().setYaw(yaw);
    }

    @Override
    public AABB getBounds() {
        return BukkitAABB.getEntityBounds(entity);
    }
}
