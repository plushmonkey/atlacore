package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.PotionEffect;
import com.plushnode.atlacore.PotionEffectType;

public class PotionEffectTypeWrapper implements PotionEffectType {
    private org.bukkit.potion.PotionEffectType type;

    public PotionEffectTypeWrapper(org.bukkit.potion.PotionEffectType type) {
        this.type = type;
    }

    public org.bukkit.potion.PotionEffectType getBukkitType() {
        return type;
    }

    @Override
    public PotionEffect createEffect(int duration, int amplifier) {
        return new PotionEffectWrapper(type.createEffect(duration, amplifier));
    }
}
