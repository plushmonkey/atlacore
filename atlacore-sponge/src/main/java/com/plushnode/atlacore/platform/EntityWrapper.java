package com.plushnode.atlacore.platform;

import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.util.AABB;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityWrapper implements Entity {
    private org.spongepowered.api.entity.Entity entity;

    public EntityWrapper(org.spongepowered.api.entity.Entity entity) {
        this.entity = entity;
    }

    public org.spongepowered.api.entity.Entity getSpongeEntity() {
        return entity;
    }

    protected void setSpongeEntity(org.spongepowered.api.entity.Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean equals(Object obj) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();

        if (obj instanceof EntityWrapper) {
            return entity.equals(((EntityWrapper)obj).entity);
        }

        return entity.equals(obj);
    }

    @Override
    public int hashCode() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();

        return entity.hashCode();
    }

    @Override
    public String toString() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();

        return entity.toString();
    }

    @Override
    public boolean addPassenger(Entity passenger) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();

        EntityWrapper wrapper = (EntityWrapper)passenger;
        return entity.addPassenger(wrapper.getSpongeEntity());
    }

    @Override
    public boolean eject() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();

        entity.clearPassengers();
        return true;
    }

    @Override
    public float getFallDistance() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        float fallDistance = 0.0f;

        if (entity.supports(Keys.FALL_DISTANCE)) {
            Optional<Float> result = entity.get(Keys.FALL_DISTANCE);
            if (result.isPresent()) {
                fallDistance = result.get();
            }
        }

        return fallDistance;
    }

    @Override
    public int getFireTicks() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        int fireTicks = 0;

        if (entity.supports(Keys.FIRE_TICKS)) {
            Optional<Integer> result = entity.get(Keys.FIRE_TICKS);
            if (result.isPresent()) {
                fireTicks = result.get();
            }
        }

        return fireTicks;
    }

    @Override
    public double getHeight() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        double height = 0.0;

        if (entity.supports(Keys.HEIGHT)) {
            Optional<Float> result = entity.get(Keys.HEIGHT);
            if (result.isPresent()) {
                height = result.get();
            }
        }

        return height;
    }

    @Override
    public Location getLocation() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return new LocationWrapper(entity.getLocation());
    }

    @Override
    public int getMaxFireTicks() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        int maxFireTicks = 0;

        if (entity.supports(Keys.MAX_BURN_TIME)) {
            Optional<Integer> result = entity.get(Keys.MAX_BURN_TIME);
            if (result.isPresent()) {
                maxFireTicks = result.get();
            }
        }

        return maxFireTicks;
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        double radius = Math.max(x, Math.max(y, z));
        Collection<org.spongepowered.api.entity.Entity> entities = entity.getNearbyEntities(radius);

        return entities.stream().map(EntityWrapper::new).collect(Collectors.toList());
    }

    @Override
    public List<Entity> getPassengers() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        List<org.spongepowered.api.entity.Entity> entities = entity.getPassengers();

        return entities.stream().map(EntityWrapper::new).collect(Collectors.toList());
    }

    @Override
    public UUID getUniqueId() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return entity.getUniqueId();
    }

    @Override
    public Entity getVehicle() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        Optional<org.spongepowered.api.entity.Entity> vehicle = entity.getVehicle();

        if (vehicle.isPresent()) {
            return new EntityWrapper(vehicle.get());
        }

        return null;
    }

    @Override
    public Vector3D getVelocity() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        Vector3d v = entity.getVelocity();
        return new Vector3D(v.getX(), v.getY(), v.getZ());
    }

    @Override
    public double getWidth() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        Optional<AABB> result = entity.getBoundingBox();

        if (!result.isPresent()) {
            return 0.0;
        }

        AABB bounds = result.get();
        return bounds.getSize().getX();
    }

    @Override
    public World getWorld() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return new WorldWrapper(entity.getWorld());
    }

    @Override
    public boolean hasGravity() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        boolean hasGravity = true;

        if (entity.supports(Keys.HAS_GRAVITY)) {
            Optional<Boolean> result = entity.get(Keys.HAS_GRAVITY);
            if (result.isPresent()) {
                hasGravity = result.get();
            }
        }

        return hasGravity;
    }

    @Override
    public boolean isDead() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        boolean isDead = false;

        if (entity.supports(Keys.HEALTH)) {
            Optional<Double> result = entity.get(Keys.HEALTH);
            if (result.isPresent()) {
                isDead = result.get() <= 0.0;
            }
        }

        return isDead;
    }

    @Override
    public boolean isEmpty() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return entity.getPassengers().isEmpty();
    }

    @Override
    public boolean isGlowing() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        boolean isGlowing = false;

        if (entity.supports(Keys.GLOWING)) {
            Optional<Boolean> result = entity.get(Keys.GLOWING);
            if (result.isPresent()) {
                isGlowing = result.get();
            }
        }

        return isGlowing;
    }

    @Override
    public boolean isInsideVehicle() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return entity.getVehicle().isPresent();
    }

    @Override
    public boolean isInvulnerable() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        boolean isInvulnerable = false;

        if (entity.supports(Keys.INVULNERABILITY_TICKS)) {
            Optional<Integer> result = entity.get(Keys.INVULNERABILITY_TICKS);
            if (result.isPresent()) {
                isInvulnerable = result.get() > 0;
            }
        }

        return isInvulnerable;
    }

    @Override
    public boolean isOnGround() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return entity.isOnGround();
    }

    @Override
    public boolean isSilent() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        boolean isSilent = false;

        if (entity.supports(Keys.IS_SILENT)) {
            Optional<Boolean> result = entity.get(Keys.IS_SILENT);
            if (result.isPresent()) {
                isSilent = result.get();
            }
        }

        return isSilent;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean leaveVehicle() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return entity.setVehicle(null);
    }

    @Override
    public void remove() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.remove();
    }

    @Override
    public boolean removePassenger(Entity passenger) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        EntityWrapper wrapper = (EntityWrapper)passenger;
        entity.removePassenger(wrapper.getSpongeEntity());
        return true;
    }

    @Override
    public void setFallDistance(float distance) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.offer(Keys.FALL_DISTANCE, distance);
    }

    @Override
    public void setFireTicks(int ticks) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.offer(Keys.FIRE_TICKS, ticks);
    }

    @Override
    public void setGlowing(boolean glowing) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.offer(Keys.GLOWING, glowing);
    }

    @Override
    public void setGravity(boolean gravity) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.offer(Keys.HAS_GRAVITY, gravity);
    }

    @Override
    public void setInvulnerable(boolean flag) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.offer(Keys.INVULNERABILITY_TICKS, 100);
    }

    @Override
    public void setSilent(boolean flag) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.offer(Keys.IS_SILENT, flag);
    }

    @Override
    public void setTickLived(int value) {

    }

    @Override
    public void setVelocity(Vector3D velocity) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        entity.setVelocity(SpongeTypeUtil.adapt(velocity));
    }

    @Override
    public boolean teleport(Entity destination) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        EntityWrapper wrapper = (EntityWrapper)destination;

        return entity.setLocation(entity.getLocation());
    }

    @Override
    public boolean teleport(Location location) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        LocationWrapper wrapper = (LocationWrapper)location;
        return entity.setLocation(wrapper.getSpongeLocation());
    }

    @Override
    public Vector3D getDirection() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        double pitch = Math.toRadians(entity.getRotation().getX());
        double yaw = Math.toRadians(entity.getRotation().getY());

        return new Vector3D(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
    }

    @Override
    public void setDirection(Vector3D direction) {
        double tau = 2 * Math.PI;
        double x = direction.getX();
        double z = direction.getZ();

        if (x == 0 && z == 0) {
            setPitch(direction.getY() > 0 ? -90 : 90);
            return;
        }

        double theta = Math.atan2(-x, z);
        float yaw = (float)Math.toDegrees((theta + tau) % tau);
        float pitch = (float)Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(x*x + z*z)));

        setYaw(yaw);
        setPitch(pitch);
    }

    @Override
    public float getPitch() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return (float)entity.getRotation().getX();
    }

    @Override
    public float getYaw() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        return (float)entity.getRotation().getY();
    }

    @Override
    public void setPitch(float pitch) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        Vector3d rot = entity.getRotation();
        Vector3d newRot = new Vector3d(pitch, rot.getY(), rot.getZ());
        entity.setRotation(newRot);
    }

    @Override
    public void setYaw(float yaw) {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        Vector3d rot = entity.getRotation();
        Vector3d newRot = new Vector3d(rot.getX(), yaw, rot.getZ());
        entity.setRotation(newRot);
    }

    @Override
    public com.plushnode.atlacore.collision.geometry.AABB getBounds() {
        org.spongepowered.api.entity.Entity entity = getSpongeEntity();
        Optional<org.spongepowered.api.util.AABB> result = entity.getBoundingBox();

        if (result.isPresent()) {
            Vector3D min = SpongeTypeUtil.adapt(result.get().getMin()).subtract(getLocation().toVector());
            Vector3D max = SpongeTypeUtil.adapt(result.get().getMax()).subtract(getLocation().toVector());
            return new com.plushnode.atlacore.collision.geometry.AABB(min, max);
        }

        return new com.plushnode.atlacore.collision.geometry.AABB(null, null);
    }

    @Override
    public boolean hasBounds() {
        return getBounds().max() != null;
    }
}
