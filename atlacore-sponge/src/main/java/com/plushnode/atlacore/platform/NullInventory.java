package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Material;

public class NullInventory implements Inventory {
    @Override
    public boolean addItem(ItemStack item) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(ItemStack item) {
        return false;
    }

    @Override
    public boolean contains(Material itemType) {
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean containsAtLeast(Material itemType, int amount) {
        return false;
    }

    @Override
    public ItemStack getMainHand() {
        return null;
    }

    @Override
    public ItemStack getOffHand() {
        return null;
    }

    @Override
    public void setMainHand(ItemStack item) {

    }

    @Override
    public void setOffHand(ItemStack item) {

    }

    @Override
    public ItemStack[] getArmorContents() {
        return new ItemStack[0];
    }

    @Override
    public ItemStack getHelmet() {
        return null;
    }

    @Override
    public ItemStack getChestplate() {
        return null;
    }

    @Override
    public ItemStack getLeggings() {
        return null;
    }

    @Override
    public ItemStack getBoots() {
        return null;
    }

    @Override
    public void setArmorContents(ItemStack[] items) {

    }

    @Override
    public void setHelmet(ItemStack item) {

    }

    @Override
    public void setChestplate(ItemStack item) {

    }

    @Override
    public void setLeggings(ItemStack item) {

    }

    @Override
    public void setBoots(ItemStack item) {

    }

    @Override
    public ItemSnapshot getHelmetSnapshot() {
        return null;
    }

    @Override
    public ItemSnapshot getChestplateSnapshot() {
        return null;
    }

    @Override
    public ItemSnapshot getLeggingsSnapshot() {
        return null;
    }

    @Override
    public ItemSnapshot getBootsSnapshot() {
        return null;
    }

    @Override
    public void setHelmet(ItemSnapshot item) {

    }

    @Override
    public void setChestplate(ItemSnapshot item) {

    }

    @Override
    public void setLeggings(ItemSnapshot item) {

    }

    @Override
    public void setBoots(ItemSnapshot item) {

    }

    @Override
    public void removeAll(ItemStack item) {

    }

    @Override
    public void removeAll(Material itemType) {

    }

    @Override
    public boolean removeAmount(ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean removeAmount(Material itemType, int amount) {
        return false;
    }
}
