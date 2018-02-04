package com.plushnode.atlacore.util;

import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PotionUtil {
    private static Map<com.plushnode.atlacore.platform.PotionEffectType, PotionEffectType> mapping = new HashMap<>();

    static {
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.BLINDNESS, PotionEffectType.BLINDNESS);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.NAUSEA, PotionEffectType.CONFUSION);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.HASTE, PotionEffectType.FAST_DIGGING);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.GLOWING, PotionEffectType.GLOWING);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.INSTANT_DAMAGE, PotionEffectType.HARM);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.INSTANT_HEALTH, PotionEffectType.HEAL);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.HEALTH_BOOST, PotionEffectType.HEALTH_BOOST);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.HUNGER, PotionEffectType.HUNGER);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.STRENGTH, PotionEffectType.INCREASE_DAMAGE);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.INVISIBILITY, PotionEffectType.INVISIBILITY);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.JUMP_BOOST, PotionEffectType.JUMP);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.LEVITATION, PotionEffectType.LEVITATION);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.LUCK, PotionEffectType.LUCK);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.NIGHT_VISION, PotionEffectType.NIGHT_VISION);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.POISON, PotionEffectType.POISON);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.REGENERATION, PotionEffectType.REGENERATION);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.SATURATION, PotionEffectType.SATURATION);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.SLOWNESS, PotionEffectType.SLOW);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.MINING_FATIGUE, PotionEffectType.SLOW_DIGGING);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.SPEED, PotionEffectType.SPEED);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.UNLUCK, PotionEffectType.UNLUCK);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.WATER_BREATHING, PotionEffectType.WATER_BREATHING);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.WEAKNESS, PotionEffectType.WEAKNESS);
        mapping.put(com.plushnode.atlacore.platform.PotionEffectType.WITHER, PotionEffectType.WITHER);
    }

    private PotionUtil() {

    }

    public static PotionEffectType toBukkit(com.plushnode.atlacore.platform.PotionEffectType type) {
        PotionEffectType result = mapping.get(type);

        if (result == null) {
            return PotionEffectType.ABSORPTION;
        }

        return result;
    }

    public static com.plushnode.atlacore.platform.PotionEffectType fromBukkit(PotionEffectType type) {
        Optional<com.plushnode.atlacore.platform.PotionEffectType> result = mapping.entrySet().stream()
                .filter((e) -> e.getValue() == type)
                .map(Map.Entry::getKey)
                .findAny();

        if (!result.isPresent()) {
            return com.plushnode.atlacore.platform.PotionEffectType.ABSORPTION;
        }

        return result.get();
    }
}
