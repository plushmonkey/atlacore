package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.PotionUtil;
import com.plushnode.atlacore.util.TypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LivingEntityWrapper extends EntityWrapper implements LivingEntity {
    public LivingEntityWrapper(org.bukkit.entity.LivingEntity entity) {
        super(entity);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LivingEntityWrapper) {
            return entity.equals(((LivingEntityWrapper)obj).entity);
        }
        return entity.equals(obj);
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }

    @Override
    public void damage(double amount) {
        ((org.bukkit.entity.LivingEntity)entity).damage(amount);
    }

    @Override
    public void damage(double amount, Entity source) {
        EntityWrapper wrapper = (EntityWrapper)source;
        ((org.bukkit.entity.LivingEntity)entity).damage(amount, wrapper.getBukkitEntity());
    }

    @Override
    public double getHealth() {
        return ((org.bukkit.entity.LivingEntity)entity).getHealth();
    }

    @Override
    public double getMaxHealth() {
        return ((org.bukkit.entity.LivingEntity)entity).getMaxHealth();
    }

    @Override
    public void resetMaxHealth() {
        ((org.bukkit.entity.LivingEntity)entity).resetMaxHealth();
    }

    @Override
    public void setHealth(double health) {
        ((org.bukkit.entity.LivingEntity)entity).setHealth(health);
    }

    @Override
    public void setMaxHealth(double health) {
        ((org.bukkit.entity.LivingEntity)entity).setMaxHealth(health);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        PotionEffectWrapper wrapper = (PotionEffectWrapper)effect;
        return ((org.bukkit.entity.LivingEntity)entity).addPotionEffect(wrapper.getBukkitEffect());
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        PotionEffectWrapper wrapper = (PotionEffectWrapper)effect;
        return ((org.bukkit.entity.LivingEntity)entity).addPotionEffect(wrapper.getBukkitEffect(), force);
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        Collection<org.bukkit.potion.PotionEffect> bukkitEffects =
                effects.stream().map(e -> ((PotionEffectWrapper)e).getBukkitEffect())
                        .collect(Collectors.toCollection(ArrayList::new));

        return ((org.bukkit.entity.LivingEntity)entity).addPotionEffects(bukkitEffects);
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return ((org.bukkit.entity.LivingEntity)entity).getActivePotionEffects().stream()
                .map(PotionEffectWrapper::new).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean getCanPickupItems() {
        return ((org.bukkit.entity.LivingEntity)entity).getCanPickupItems();
    }

    @Override
    public double getEyeHeight() {
        return ((org.bukkit.entity.LivingEntity)entity).getEyeHeight();
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        return ((org.bukkit.entity.LivingEntity)entity).getEyeHeight(ignoreSneaking);
    }

    @Override
    public Location getEyeLocation() {
        return new LocationWrapper(((org.bukkit.entity.LivingEntity)entity).getEyeLocation());
    }

    @Override
    public double getLastDamage() {
        return ((org.bukkit.entity.LivingEntity)entity).getLastDamage();
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        Set<org.bukkit.Material> bukkitMaterials = transparent.stream().map(TypeUtil::adapt).collect(Collectors.toSet());
        List<org.bukkit.block.Block> blocks = ((org.bukkit.entity.LivingEntity)entity).getLineOfSight(bukkitMaterials, maxDistance);

        return blocks.stream().map(BlockWrapper::new).collect(Collectors.toList());
    }

    @Override
    public int getMaximumAir() {
        return ((org.bukkit.entity.LivingEntity)entity).getMaximumAir();
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return ((org.bukkit.entity.LivingEntity)entity).getMaximumNoDamageTicks();
    }

    @Override
    public int getNoDamageTicks() {
        return ((org.bukkit.entity.LivingEntity)entity).getNoDamageTicks();
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type) {
        org.bukkit.potion.PotionEffect effect = ((org.bukkit.entity.LivingEntity)entity).getPotionEffect(PotionUtil.toBukkit(type));

        if (effect == null) {
            return null;
        }

        return new PotionEffectWrapper(effect);
    }

    @Override
    public int getRemainingAir() {
        return ((org.bukkit.entity.LivingEntity)entity).getRemainingAir();
    }

    @Override
    public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        Set<org.bukkit.Material> bukkitMaterials = transparent.stream().map(TypeUtil::adapt).collect(Collectors.toSet());
        org.bukkit.block.Block block = ((org.bukkit.entity.LivingEntity)entity).getTargetBlock(bukkitMaterials, maxDistance);

        return new BlockWrapper(block);
    }

    @Override
    public boolean hasAI() {
        return ((org.bukkit.entity.LivingEntity)entity).hasAI();
    }

    @Override
    public boolean hasLineOfSight(Entity other) {
        EntityWrapper wrapper = (EntityWrapper)other;
        return ((org.bukkit.entity.LivingEntity)entity).hasLineOfSight(wrapper.getBukkitEntity());
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return ((org.bukkit.entity.LivingEntity)entity).hasPotionEffect(PotionUtil.toBukkit(type));
    }

    @Override
    public boolean isCollidable() {
        return ((org.bukkit.entity.LivingEntity)entity).isCollidable();
    }

    @Override
    public boolean isGliding() {
        return ((org.bukkit.entity.LivingEntity)entity).isGliding();
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        ((org.bukkit.entity.LivingEntity)entity).removePotionEffect(PotionUtil.toBukkit(type));
    }

    @Override
    public void setAI(boolean ai) {
        ((org.bukkit.entity.LivingEntity)entity).setAI(ai);
    }

    @Override
    public void setCanPickupItems(boolean p) {
        ((org.bukkit.entity.LivingEntity)entity).setCanPickupItems(p);
    }

    @Override
    public void setCollidable(boolean c) {
        ((org.bukkit.entity.LivingEntity)entity).setCollidable(c);
    }

    @Override
    public void setGliding(boolean g) {
        ((org.bukkit.entity.LivingEntity)entity).setGliding(g);
    }

    @Override
    public void setLastDamage(double damage) {
        ((org.bukkit.entity.LivingEntity)entity).setLastDamage(damage);
    }

    @Override
    public void setMaximumAir(int ticks) {
        ((org.bukkit.entity.LivingEntity)entity).setMaximumAir(ticks);
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        ((org.bukkit.entity.LivingEntity)entity).setMaximumNoDamageTicks(ticks);
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        ((org.bukkit.entity.LivingEntity)entity).setNoDamageTicks(ticks);
    }

    @Override
    public void setRemainingAir(int ticks) {
        ((org.bukkit.entity.LivingEntity)entity).setRemainingAir(ticks);
    }

    @Override
    public Vector3D getDirection() {
        org.bukkit.Location loc = ((LocationWrapper)getEyeLocation()).getBukkitLocation();
        return TypeUtil.adapt(loc.getDirection());
    }
}
