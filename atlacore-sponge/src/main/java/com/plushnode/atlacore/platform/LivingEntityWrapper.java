package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.PotionUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.type.PickupRules;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;

import java.util.*;

public class LivingEntityWrapper extends EntityWrapper implements LivingEntity {
    public LivingEntityWrapper(org.spongepowered.api.entity.Entity entity) {
        super(entity);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return effect.apply(this);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        return addPotionEffect(effect);
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            addPotionEffect(effect);
        }
        return true;
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        List<PotionEffect> result = new ArrayList<>();
        PotionEffectData effects = getSpongeEntity().getOrCreate(PotionEffectData.class).get();

        for (org.spongepowered.api.effect.potion.PotionEffect effect : effects.asList()) {
            result.add(new PotionEffectWrapper(effect));
        }

        return result;
    }

    @Override
    public boolean getCanPickupItems() {
        return getSpongeEntity().get(Keys.PICKUP_RULE).orElse(PickupRules.ALLOWED) == PickupRules.ALLOWED;
    }

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        return 0;
    }

    @Override
    public Location getEyeLocation() {
        // todo: implement this. This only works for normal players.
        return getLocation().add(0, 1.8, 0);
    }

    @Override
    public double getLastDamage() {
        return getSpongeEntity().get(Keys.LAST_DAMAGE).orElse(Optional.of(0.0)).get();
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public int getMaximumAir() {
        return getSpongeEntity().get(Keys.MAX_AIR).orElse(0);
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return 20;
    }

    @Override
    public int getNoDamageTicks() {
        return getSpongeEntity().get(Keys.INVULNERABILITY_TICKS).orElse(0);
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type) {
        PotionEffectData effects = getSpongeEntity().getOrCreate(PotionEffectData.class).get();

        org.spongepowered.api.effect.potion.PotionEffect effect = effects.asList().stream()
                .filter((pe) -> pe.getType().equals(PotionUtil.toSponge(type)))
                .findAny().orElse(null);

        if (effect == null) {
            return null;
        }

        return new PotionEffectWrapper(effect);
    }

    @Override
    public int getRemainingAir() {
        return getSpongeEntity().get(Keys.REMAINING_AIR).orElse(0);
    }

    @Override
    public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public boolean hasAI() {
        return getSpongeEntity().get(Keys.AI_ENABLED).orElse(false);
    }

    @Override
    public boolean hasLineOfSight(Entity other) {
        return false;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return getPotionEffect(type) != null;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public boolean isGliding() {
        return false;
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        PotionEffectData effects = getSpongeEntity().getOrCreate(PotionEffectData.class).get();

        effects.removeAll((pe) -> pe.getType().equals(PotionUtil.toSponge(type)));

        getSpongeEntity().offer(effects);
    }

    @Override
    public void setAI(boolean ai) {
        getSpongeEntity().offer(Keys.AI_ENABLED, ai);
    }

    @Override
    public void setCanPickupItems(boolean p) {
        if (p) {
            getSpongeEntity().offer(Keys.PICKUP_RULE, PickupRules.ALLOWED);
        } else {
            getSpongeEntity().offer(Keys.PICKUP_RULE, PickupRules.DISALLOWED);
        }
    }

    @Override
    public void setCollidable(boolean c) {
        getSpongeEntity().offer(Keys.VANISH_IGNORES_COLLISION, c);
    }

    @Override
    public void setGliding(boolean g) {

    }

    @Override
    public void setLastDamage(double damage) {
        getSpongeEntity().offer(Keys.LAST_DAMAGE, Optional.of(damage));
    }

    @Override
    public void setMaximumAir(int ticks) {
        getSpongeEntity().offer(Keys.MAX_AIR, ticks);
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        getSpongeEntity().offer(Keys.INVULNERABILITY_TICKS, ticks);
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        getSpongeEntity().offer(Keys.INVULNERABILITY_TICKS, ticks);
    }

    @Override
    public void setRemainingAir(int ticks) {
        getSpongeEntity().offer(Keys.REMAINING_AIR, ticks);
    }

    @Override
    public void damage(double amount) {
        getSpongeEntity().damage(amount, DamageSources.GENERIC);
    }

    @Override
    public void damage(double amount, Entity source) {
        org.spongepowered.api.entity.Entity spongeEntity = ((EntityWrapper)source).getSpongeEntity();
        getSpongeEntity().damage(amount, EntityDamageSource.builder().entity(spongeEntity).type(DamageTypes.CUSTOM).build());
    }

    @Override
    public double getHealth() {
        return getSpongeEntity().get(Keys.HEALTH).orElse(0.0);
    }

    @Override
    public double getMaxHealth() {
        return getSpongeEntity().get(Keys.MAX_HEALTH).orElse(0.0);
    }

    @Override
    public void resetMaxHealth() {
        getSpongeEntity().offer(Keys.MAX_HEALTH, 20.0);
    }

    @Override
    public void setHealth(double health) {
        getSpongeEntity().offer(Keys.HEALTH, health);
    }

    @Override
    public void setMaxHealth(double health) {
        getSpongeEntity().offer(Keys.MAX_HEALTH, health);
    }
}
