package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.util.PotionUtil;

public class SpongePotionFactory implements PotionFactory {
    @Override
    public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier) {
        org.spongepowered.api.effect.potion.PotionEffect e = org.spongepowered.api.effect.potion.PotionEffect.builder()
                .potionType(PotionUtil.toSponge(type))
                .amplifier(amplifier)
                .duration(duration)
                .build();

        return new PotionEffectWrapper(e);
    }

    @Override
    public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier, boolean ambient) {
        org.spongepowered.api.effect.potion.PotionEffect e = org.spongepowered.api.effect.potion.PotionEffect.builder()
                .potionType(PotionUtil.toSponge(type))
                .amplifier(amplifier)
                .duration(duration)
                .ambience(ambient)
                .build();

        return new PotionEffectWrapper(e);
    }

    @Override
    public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        org.spongepowered.api.effect.potion.PotionEffect e = org.spongepowered.api.effect.potion.PotionEffect.builder()
                .potionType(PotionUtil.toSponge(type))
                .amplifier(amplifier)
                .duration(duration)
                .ambience(ambient)
                .particles(particles)
                .build();

        return new PotionEffectWrapper(e);
    }
}
