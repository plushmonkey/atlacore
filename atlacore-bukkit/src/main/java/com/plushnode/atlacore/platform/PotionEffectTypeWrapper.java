package com.plushnode.atlacore.platform;

public class PotionEffectTypeWrapper implements PotionEffectType {
    private org.bukkit.potion.PotionEffectType type;

    public PotionEffectTypeWrapper(org.bukkit.potion.PotionEffectType type) {
        this.type = type;
    }

    public org.bukkit.potion.PotionEffectType getBukkitType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PotionEffectTypeWrapper) {
            return type.equals(((PotionEffectTypeWrapper)obj).type);
        }
        return type.equals(obj);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public PotionEffect createEffect(int duration, int amplifier) {
        return new PotionEffectWrapper(type.createEffect(duration, amplifier));
    }
}
