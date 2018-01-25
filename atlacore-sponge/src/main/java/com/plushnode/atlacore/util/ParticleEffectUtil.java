package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Map;

public class ParticleEffectUtil {
    private static Map<ParticleEffect, ParticleType> effectMap = new HashMap<>();

    private ParticleEffectUtil() {

    }

    static {
        effectMap.put(ParticleEffect.EXPLOSION_NORMAL, ParticleTypes.EXPLOSION);
        effectMap.put(ParticleEffect.EXPLODE, ParticleTypes.EXPLOSION);
        effectMap.put(ParticleEffect.EXPLOSION_LARGE, ParticleTypes.LARGE_EXPLOSION);
        effectMap.put(ParticleEffect.LARGE_EXPLODE, ParticleTypes.LARGE_EXPLOSION);
        effectMap.put(ParticleEffect.EXPLOSION_HUGE, ParticleTypes.HUGE_EXPLOSION);
        effectMap.put(ParticleEffect.HUGE_EXPLOSION, ParticleTypes.HUGE_EXPLOSION);
        effectMap.put(ParticleEffect.FIREWORKS_SPARK, ParticleTypes.FIREWORKS_SPARK);
        effectMap.put(ParticleEffect.WATER_BUBBLE, ParticleTypes.WATER_BUBBLE);
        effectMap.put(ParticleEffect.BUBBLE, ParticleTypes.WATER_BUBBLE);
        effectMap.put(ParticleEffect.WATER_SPLASH, ParticleTypes.WATER_SPLASH);
        effectMap.put(ParticleEffect.SPLASH, ParticleTypes.WATER_SPLASH);
        effectMap.put(ParticleEffect.WATER_WAKE, ParticleTypes.WATER_WAKE);
        effectMap.put(ParticleEffect.WAKE, ParticleTypes.WATER_WAKE);
        effectMap.put(ParticleEffect.SUSPENDED, ParticleTypes.SUSPENDED);
        effectMap.put(ParticleEffect.SUSPEND, ParticleTypes.SUSPENDED);
        effectMap.put(ParticleEffect.SUSPENDED_DEPTH, ParticleTypes.SUSPENDED_DEPTH);
        effectMap.put(ParticleEffect.DEPTH_SUSPEND, ParticleTypes.SUSPENDED_DEPTH);
        effectMap.put(ParticleEffect.CRIT, ParticleTypes.CRITICAL_HIT);
        effectMap.put(ParticleEffect.MAGIC_CRIT, ParticleTypes.MAGIC_CRITICAL_HIT);
        effectMap.put(ParticleEffect.CRIT_MAGIC, ParticleTypes.MAGIC_CRITICAL_HIT);
        effectMap.put(ParticleEffect.SMOKE, ParticleTypes.SMOKE);
        effectMap.put(ParticleEffect.SMOKE_NORMAL, ParticleTypes.SMOKE);
        effectMap.put(ParticleEffect.SMOKE_LARGE, ParticleTypes.LARGE_SMOKE);
        effectMap.put(ParticleEffect.LARGE_SMOKE, ParticleTypes.LARGE_SMOKE);
        effectMap.put(ParticleEffect.SPELL, ParticleTypes.SPELL);
        effectMap.put(ParticleEffect.SPELL_INSTANT, ParticleTypes.INSTANT_SPELL);
        effectMap.put(ParticleEffect.INSTANT_SPELL, ParticleTypes.INSTANT_SPELL);
        effectMap.put(ParticleEffect.SPELL_MOB, ParticleTypes.MOB_SPELL);
        effectMap.put(ParticleEffect.MOB_SPELL, ParticleTypes.MOB_SPELL);
        effectMap.put(ParticleEffect.SPELL_MOB_AMBIENT, ParticleTypes.AMBIENT_MOB_SPELL);
        effectMap.put(ParticleEffect.MOB_SPELL_AMBIENT, ParticleTypes.AMBIENT_MOB_SPELL);
        effectMap.put(ParticleEffect.SPELL_WITCH, ParticleTypes.WITCH_SPELL);
        effectMap.put(ParticleEffect.WITCH_MAGIC, ParticleTypes.WITCH_SPELL);
        effectMap.put(ParticleEffect.DRIP_WATER, ParticleTypes.DRIP_WATER);
        effectMap.put(ParticleEffect.DRIP_LAVA, ParticleTypes.DRIP_LAVA);
        effectMap.put(ParticleEffect.VILLAGER_ANGRY, ParticleTypes.ANGRY_VILLAGER);
        effectMap.put(ParticleEffect.ANGRY_VILLAGER, ParticleTypes.ANGRY_VILLAGER);
        effectMap.put(ParticleEffect.VILLAGER_HAPPY, ParticleTypes.HAPPY_VILLAGER);
        effectMap.put(ParticleEffect.HAPPY_VILLAGER, ParticleTypes.HAPPY_VILLAGER);
        effectMap.put(ParticleEffect.TOWN_AURA, ParticleTypes.TOWN_AURA);
        effectMap.put(ParticleEffect.NOTE, ParticleTypes.NOTE);
        effectMap.put(ParticleEffect.PORTAL, ParticleTypes.PORTAL);
        effectMap.put(ParticleEffect.ENCHANTMENT_TABLE, ParticleTypes.ENCHANTING_GLYPHS);
        effectMap.put(ParticleEffect.FLAME, ParticleTypes.FLAME);
        effectMap.put(ParticleEffect.LAVA, ParticleTypes.LAVA);
        effectMap.put(ParticleEffect.FOOTSTEP, ParticleTypes.FOOTSTEP);
        effectMap.put(ParticleEffect.CLOUD, ParticleTypes.CLOUD);
        effectMap.put(ParticleEffect.REDSTONE, ParticleTypes.REDSTONE_DUST);
        effectMap.put(ParticleEffect.RED_DUST, ParticleTypes.REDSTONE_DUST);
        effectMap.put(ParticleEffect.SNOWBALL, ParticleTypes.SNOWBALL);
        effectMap.put(ParticleEffect.SNOWBALL_POOF, ParticleTypes.SNOWBALL);
        effectMap.put(ParticleEffect.SNOW_SHOVEL, ParticleTypes.SNOW_SHOVEL);
        effectMap.put(ParticleEffect.SLIME, ParticleTypes.SLIME);
        effectMap.put(ParticleEffect.HEART, ParticleTypes.HEART);
        effectMap.put(ParticleEffect.BARRIER, ParticleTypes.BARRIER);
        effectMap.put(ParticleEffect.ITEM_CRACK, ParticleTypes.ITEM_CRACK);
        effectMap.put(ParticleEffect.BLOCK_CRACK, ParticleTypes.BLOCK_CRACK);
        effectMap.put(ParticleEffect.BLOCK_DUST, ParticleTypes.BLOCK_DUST);
        effectMap.put(ParticleEffect.WATER_DROP, ParticleTypes.WATER_DROP);
        effectMap.put(ParticleEffect.ITEM_TAKE, ParticleTypes.ITEM_CRACK); // Note: unknown mapping
        effectMap.put(ParticleEffect.MOB_APPEARANCE, ParticleTypes.GUARDIAN_APPEARANCE);
        effectMap.put(ParticleEffect.END_ROD, ParticleTypes.END_ROD);
        effectMap.put(ParticleEffect.DRAGON_BREATH, ParticleTypes.DRAGON_BREATH);
        effectMap.put(ParticleEffect.DAMAGE_INDICATOR, ParticleTypes.DAMAGE_INDICATOR);
        effectMap.put(ParticleEffect.SWEEP, ParticleTypes.SWEEP_ATTACK);
        effectMap.put(ParticleEffect.FALLING_DUST, ParticleTypes.FALLING_DUST);
        effectMap.put(ParticleEffect.TOTEM, ParticleTypes.SPELL); // Note: Unknown mapping
        effectMap.put(ParticleEffect.SPIT, ParticleTypes.SPELL); // Note: Unknown mapping
    }

    public static ParticleType toParticleType(ParticleEffect effect) {
        ParticleType type = effectMap.get(effect);

        if (type == null) {
            return ParticleTypes.SPELL;
        }

        return type;
    }
}
