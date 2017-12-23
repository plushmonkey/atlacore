package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.LivingEntity;
import com.plushnode.atlacore.PotionEffect;
import com.plushnode.atlacore.PotionEffectType;

public class PotionEffectWrapper implements PotionEffect {
    private org.bukkit.potion.PotionEffect effect;

    public PotionEffectWrapper(org.bukkit.potion.PotionEffect effect) {
        this.effect = effect;
    }

    public org.bukkit.potion.PotionEffect getBukkitEffect() {
        return effect;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PotionEffectWrapper) {
            return effect.equals(((PotionEffectWrapper)obj).effect);
        }
        return effect.equals(obj);
    }

    @Override
    public int hashCode() {
        return effect.hashCode();
    }

    @Override
    public boolean apply(LivingEntity entity) {
        LivingEntityWrapper wrapper = (LivingEntityWrapper)entity;
        return effect.apply((org.bukkit.entity.LivingEntity)wrapper.getBukkitEntity());
    }

    @Override
    public int getAmplifier() {
        return effect.getAmplifier();
    }

    @Override
    public int getDuration() {
        return effect.getDuration();
    }

    @Override
    public PotionEffectType getType() {
        return new PotionEffectTypeWrapper(effect.getType());
    }

    @Override
    public boolean hasParticles() {
        return effect.hasParticles();
    }

    @Override
    public boolean isAmbient() {
        return effect.isAmbient();
    }
}
