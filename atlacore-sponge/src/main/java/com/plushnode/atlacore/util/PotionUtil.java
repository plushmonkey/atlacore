package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PotionUtil {
    private static Map<PotionEffectType, org.spongepowered.api.effect.potion.PotionEffectType> mapping = new HashMap<>();

    static {
        mapping.put(PotionEffectType.ABSORPTION, PotionEffectTypes.ABSORPTION);
        mapping.put(PotionEffectType.BLINDNESS, PotionEffectTypes.BLINDNESS);
        mapping.put(PotionEffectType.NAUSEA, PotionEffectTypes.NAUSEA);
        mapping.put(PotionEffectType.RESISTANCE, PotionEffectTypes.RESISTANCE);
        mapping.put(PotionEffectType.HASTE, PotionEffectTypes.HASTE);
        mapping.put(PotionEffectType.FIRE_RESISTANCE, PotionEffectTypes.FIRE_RESISTANCE);
        mapping.put(PotionEffectType.GLOWING, PotionEffectTypes.GLOWING);
        mapping.put(PotionEffectType.INSTANT_DAMAGE, PotionEffectTypes.INSTANT_DAMAGE);
        mapping.put(PotionEffectType.INSTANT_HEALTH, PotionEffectTypes.INSTANT_HEALTH);
        mapping.put(PotionEffectType.HEALTH_BOOST, PotionEffectTypes.HEALTH_BOOST);
        mapping.put(PotionEffectType.HUNGER, PotionEffectTypes.HUNGER);
        mapping.put(PotionEffectType.STRENGTH, PotionEffectTypes.STRENGTH);
        mapping.put(PotionEffectType.INVISIBILITY, PotionEffectTypes.INVISIBILITY);
        mapping.put(PotionEffectType.JUMP_BOOST, PotionEffectTypes.JUMP_BOOST);
        mapping.put(PotionEffectType.LEVITATION, PotionEffectTypes.LEVITATION);
        mapping.put(PotionEffectType.LUCK, PotionEffectTypes.LUCK);
        mapping.put(PotionEffectType.NIGHT_VISION, PotionEffectTypes.NIGHT_VISION);
        mapping.put(PotionEffectType.POISON, PotionEffectTypes.POISON);
        mapping.put(PotionEffectType.REGENERATION, PotionEffectTypes.REGENERATION);
        mapping.put(PotionEffectType.SATURATION, PotionEffectTypes.SATURATION);
        mapping.put(PotionEffectType.SLOWNESS, PotionEffectTypes.SLOWNESS);
        mapping.put(PotionEffectType.MINING_FATIGUE, PotionEffectTypes.MINING_FATIGUE);
        mapping.put(PotionEffectType.SPEED, PotionEffectTypes.SPEED);
        mapping.put(PotionEffectType.UNLUCK, PotionEffectTypes.UNLUCK);
        mapping.put(PotionEffectType.WATER_BREATHING, PotionEffectTypes.WATER_BREATHING);
        mapping.put(PotionEffectType.WEAKNESS, PotionEffectTypes.WEAKNESS);
        mapping.put(PotionEffectType.WITHER, PotionEffectTypes.WITHER);
    }

    private PotionUtil() {

    }

    public static org.spongepowered.api.effect.potion.PotionEffectType toSponge(PotionEffectType type) {
        org.spongepowered.api.effect.potion.PotionEffectType result = mapping.get(type);

        if (result == null) {
            return PotionEffectTypes.ABSORPTION;
        }

        return result;
    }

    public static PotionEffectType fromSponge(org.spongepowered.api.effect.potion.PotionEffectType type) {
        Optional<PotionEffectType> result = mapping.entrySet().stream()
                .filter((e) -> e.getValue() == type)
                .map(Map.Entry::getKey)
                .findAny();

        if (!result.isPresent()) {
            return PotionEffectType.ABSORPTION;
        }

        return result.get();
    }
}
