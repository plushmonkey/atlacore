package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.Material;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LivingEntityWrapper extends EntityWrapper implements LivingEntity {
    public LivingEntityWrapper(org.spongepowered.api.entity.Entity entity) {
        super(entity);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return false;
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        return false;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        return false;
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }

    @Override
    public boolean getCanPickupItems() {
        return false;
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
        return 0;
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public int getMaximumAir() {
        return 0;
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return 0;
    }

    @Override
    public int getNoDamageTicks() {
        return 0;
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type) {
        return null;
    }

    @Override
    public int getRemainingAir() {
        return 0;
    }

    @Override
    public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public boolean hasAI() {
        return false;
    }

    @Override
    public boolean hasLineOfSight(Entity other) {
        return false;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return false;
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

    }

    @Override
    public void setAI(boolean ai) {

    }

    @Override
    public void setCanPickupItems(boolean p) {

    }

    @Override
    public void setCollidable(boolean c) {

    }

    @Override
    public void setGliding(boolean g) {

    }

    @Override
    public void setLastDamage(double damage) {

    }

    @Override
    public void setMaximumAir(int ticks) {

    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {

    }

    @Override
    public void setNoDamageTicks(int ticks) {

    }

    @Override
    public void setRemainingAir(int ticks) {

    }

    @Override
    public void damage(double amount) {

    }

    @Override
    public void damage(double amount, Entity source) {

    }

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public void resetMaxHealth() {

    }

    @Override
    public void setHealth(double health) {

    }

    @Override
    public void setMaxHealth(double health) {

    }
}
