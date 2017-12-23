package com.plushnode.atlacore;

import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.Material;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface LivingEntity extends Entity, Damageable {
    boolean addPotionEffect(PotionEffect effect);
    boolean addPotionEffect(PotionEffect effect, boolean force);
    boolean addPotionEffects(Collection<PotionEffect> effects);
    Collection<PotionEffect> getActivePotionEffects();
    boolean getCanPickupItems();
    double getEyeHeight();
    double getEyeHeight(boolean ignoreSneaking);
    Location getEyeLocation();
    double getLastDamage();
    List<Block> getLineOfSight(Set<Material> transparent, int maxDistance);
    int getMaximumAir();
    int getMaximumNoDamageTicks();
    int getNoDamageTicks();
    PotionEffect getPotionEffect(PotionEffectType type);
    int getRemainingAir();
    Block getTargetBlock(Set<Material> transparent, int maxDistance);
    boolean hasAI();
    boolean hasLineOfSight(Entity other);
    boolean hasPotionEffect(PotionEffectType type);
    boolean isCollidable();
    boolean isGliding();
    void removePotionEffect(PotionEffectType type);
    void setAI(boolean ai);
    void setCanPickupItems(boolean p);
    void setCollidable(boolean c);
    void setGliding(boolean g);
    void setLastDamage(double damage);
    void setMaximumAir(int ticks);
    void setMaximumNoDamageTicks(int ticks);
    void setNoDamageTicks(int ticks);
    void setRemainingAir(int ticks);
}
