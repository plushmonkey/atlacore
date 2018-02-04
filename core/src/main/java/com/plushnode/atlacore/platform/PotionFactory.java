package com.plushnode.atlacore.platform;

public interface PotionFactory {
    PotionEffect createEffect(PotionEffectType type, int duration, int amplifier);
    PotionEffect createEffect(PotionEffectType type, int duration, int amplifier, boolean ambient);
    PotionEffect createEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles);
}
