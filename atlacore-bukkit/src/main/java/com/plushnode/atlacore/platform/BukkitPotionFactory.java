package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.util.PotionUtil;

public class BukkitPotionFactory implements PotionFactory {
    @Override
    public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier) {
        return new PotionEffectWrapper(new org.bukkit.potion.PotionEffect(PotionUtil.toBukkit(type), duration, amplifier));
    }

    @Override
    public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier, boolean ambient) {
        return new PotionEffectWrapper(new org.bukkit.potion.PotionEffect(PotionUtil.toBukkit(type), duration, amplifier, ambient));
    }

    @Override
    public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        return new PotionEffectWrapper(new org.bukkit.potion.PotionEffect(PotionUtil.toBukkit(type), duration, amplifier, ambient, particles));
    }
}
