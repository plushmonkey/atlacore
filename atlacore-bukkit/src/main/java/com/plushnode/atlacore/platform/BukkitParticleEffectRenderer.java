package com.plushnode.atlacore.platform;

import org.bukkit.*;

public class BukkitParticleEffectRenderer implements ParticleEffectRenderer {
    @Override
    public void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
        if (effect.getRequiresData()) {
            return;
        }

        org.bukkit.Location bukkitCenter = ((LocationWrapper)center).getBukkitLocation();

        if (effect.getRequiresWater() && !isWater(bukkitCenter)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }


        Particle particle = mapParticleType(effect);
        org.bukkit.World world = ((WorldWrapper)center.getWorld()).getBukkitWorld();

        if (effect == ParticleEffect.REDSTONE || effect == ParticleEffect.RED_DUST) {
            Color color = Color.fromRGB((int)(offsetX), (int)(offsetY), (int)(offsetZ));
            world.spawnParticle(particle, ((LocationWrapper) center).getBukkitLocation(), amount, offsetX, offsetY, offsetZ, speed, new Particle.DustOptions(color, 1));
        } else {
            world.spawnParticle(particle, ((LocationWrapper) center).getBukkitLocation(), amount, offsetX, offsetY, offsetZ, speed);
        }
    }

    @Override
    public void displayColored(ParticleEffect effect, int red, int green, int blue, float speed, int amount, Location center, double range) {
        display(effect, (float)red, (float)green, (float)blue, speed, amount, center, range);
    }

    private static boolean isWater(org.bukkit.Location location) {
        Material type = location.getBlock().getType();

        return type == Material.WATER;
    }

    private Particle mapParticleType(ParticleEffect effect) {
        switch (effect) {
            case EXPLOSION_NORMAL:
            case EXPLODE:
                return Particle.EXPLOSION_NORMAL;
            case EXPLOSION_LARGE:
            case LARGE_EXPLODE:
                return Particle.EXPLOSION_LARGE;
            case HUGE_EXPLOSION:
            case EXPLOSION_HUGE:
                return Particle.EXPLOSION_HUGE;
            case FIREWORKS_SPARK:
                return Particle.FIREWORKS_SPARK;
            case WATER_BUBBLE:
            case BUBBLE:
                return Particle.WATER_BUBBLE;
            case SPLASH:
            case WATER_SPLASH:
                return Particle.WATER_SPLASH;
            case WATER_WAKE:
            case WAKE:
                return Particle.WATER_WAKE;
            case SUSPEND:
            case SUSPENDED:
                return Particle.SUSPENDED;
            case SUSPENDED_DEPTH:
            case DEPTH_SUSPEND:
                return Particle.SUSPENDED_DEPTH;
            case CRIT:
                return Particle.CRIT;
            case CRIT_MAGIC:
            case MAGIC_CRIT:
                return Particle.CRIT_MAGIC;
            case SMOKE:
            case SMOKE_NORMAL:
                return Particle.SMOKE_NORMAL;
            case SMOKE_LARGE:
            case LARGE_SMOKE:
                return Particle.SMOKE_LARGE;
            case SPELL:
                return Particle.SPELL;
            case SPELL_INSTANT:
            case INSTANT_SPELL:
                return Particle.SPELL_INSTANT;
            case SPELL_MOB:
            case MOB_SPELL:
                return Particle.SPELL_MOB;
            case MOB_SPELL_AMBIENT:
            case SPELL_MOB_AMBIENT:
                return Particle.SPELL_MOB_AMBIENT;
            case SPELL_WITCH:
            case WITCH_MAGIC:
                return Particle.SPELL_WITCH;
            case DRIP_WATER:
                return Particle.DRIP_WATER;
            case DRIP_LAVA:
                return Particle.DRIP_LAVA;
            case ANGRY_VILLAGER:
            case VILLAGER_ANGRY:
                return Particle.VILLAGER_ANGRY;
            case VILLAGER_HAPPY:
            case HAPPY_VILLAGER:
                return Particle.VILLAGER_HAPPY;
            case TOWN_AURA:
                return Particle.TOWN_AURA;
            case NOTE:
                return Particle.NOTE;
            case PORTAL:
                return Particle.PORTAL;
            case ENCHANTMENT_TABLE:
                return Particle.ENCHANTMENT_TABLE;
            case FLAME:
                return Particle.FLAME;
            case LAVA:
                return Particle.LAVA;
            case CLOUD:
                return Particle.CLOUD;
            case RED_DUST:
            case REDSTONE:
                return Particle.REDSTONE;
            case SNOWBALL:
            case SNOWBALL_POOF:
                return Particle.SNOWBALL;
            case SNOW_SHOVEL:
                return Particle.SNOW_SHOVEL;
            case SLIME:
                return Particle.SLIME;
            case HEART:
                return Particle.HEART;
            case BARRIER:
                return Particle.BARRIER;
            case ITEM_CRACK:
                return Particle.ITEM_CRACK;
            case BLOCK_CRACK:
                return Particle.BLOCK_CRACK;
            case BLOCK_DUST:
                return Particle.BLOCK_DUST;
            case WATER_DROP:
                return Particle.WATER_DROP;
            case MOB_APPEARANCE:
                return Particle.MOB_APPEARANCE;
            case END_ROD:
                return Particle.END_ROD;
            case DRAGON_BREATH:
                return Particle.DRAGON_BREATH;
            case DAMAGE_INDICATOR:
                return Particle.DAMAGE_INDICATOR;
            case SWEEP:
                return Particle.SWEEP_ATTACK;
            case FALLING_DUST:
                return Particle.FALLING_DUST;
            case TOTEM:
                return Particle.TOTEM;
            case SPIT:
                return Particle.SPIT;
            default:
                return Particle.SPIT;
        }
    }
}
