package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.util.PotionUtil;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.entity.Entity;

public class PotionEffectWrapper implements PotionEffect {
    private org.spongepowered.api.effect.potion.PotionEffect effect;

    public PotionEffectWrapper(org.spongepowered.api.effect.potion.PotionEffect effect) {
        this.effect = effect;
    }

    public org.spongepowered.api.effect.potion.PotionEffect getSpongeEffect() {
        return effect;
    }

    @Override
    public boolean apply(LivingEntity entity) {
        Entity spongeEntity = ((LivingEntityWrapper)entity).getSpongeEntity();
        PotionEffectData effects = spongeEntity.getOrCreate(PotionEffectData.class).get();

        effects.addElement(effect);

        spongeEntity.offer(effects);

        return true;
    }

    @Override
    public int getAmplifier() {
        // Return it with +1 because that's how Bukkit does it.
        return effect.getAmplifier() + 1;
    }

    @Override
    public int getDuration() {
        return effect.getDuration();
    }

    @Override
    public PotionEffectType getType() {
        return PotionUtil.fromSponge(effect.getType());
    }

    @Override
    public boolean hasParticles() {
        return effect.getShowParticles();
    }

    @Override
    public boolean isAmbient() {
        return effect.isAmbient();
    }
}
