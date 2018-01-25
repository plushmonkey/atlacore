package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.PotionEffect;

public interface PotionEffectType {
    PotionEffect createEffect(int duration, int amplifier);
}
