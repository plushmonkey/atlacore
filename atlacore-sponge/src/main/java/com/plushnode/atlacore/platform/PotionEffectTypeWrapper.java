package com.plushnode.atlacore.platform;

public class PotionEffectTypeWrapper implements PotionEffectType {
    private org.spongepowered.api.effect.potion.PotionEffectType type;

    public PotionEffectTypeWrapper(org.spongepowered.api.effect.potion.PotionEffectType type) {
        this.type = type;
    }

    public org.spongepowered.api.effect.potion.PotionEffectType getSpongeType() {
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
        org.spongepowered.api.effect.potion.PotionEffect potion = org.spongepowered.api.effect.potion.PotionEffect.builder()
                .potionType(type)
                .duration(duration)
                .amplifier(amplifier)
                .build();

        return new PotionEffectWrapper(potion);
    }
}
