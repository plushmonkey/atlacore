package com.plushnode.atlacore.platform;


import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.material.SpongeMaterialUtil;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.ParticleEffectUtil;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;

public class SpongeParticleEffectRenderer implements ParticleEffectRenderer {
    @Override
    public void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center) {
        display(effect, offsetX, offsetY, offsetZ, speed, amount, center, null);
    }

    @Override
    public void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, Material material) {
        ParticleType type = ParticleEffectUtil.toParticleType(effect);

        if (amount <= 0) amount = 1;

        BlockState blockState = SpongeMaterialUtil.toBlockType(material).getDefaultState();

        org.spongepowered.api.effect.particle.ParticleEffect.Builder builder = org.spongepowered.api.effect.particle.ParticleEffect.builder()
                .type(type)
                .quantity(amount)
                .offset(new Vector3d(offsetX, offsetY, offsetZ))
                .velocity(new Vector3d(speed, speed, speed));

        if (material != null) {
            if (effect == ParticleEffect.BLOCK_CRACK) {
                builder = builder.option(ParticleOptions.BLOCK_STATE, blockState);
            } else if (effect == ParticleEffect.ITEM_CRACK) {
                ItemType itemType = SpongeMaterialUtil.toItemType(material);

                if (itemType != null) {
                    ItemStackSnapshot snapshot = itemType.getTemplate();
                    builder = builder.option(ParticleOptions.ITEM_STACK_SNAPSHOT, snapshot);
                }
            }
        }

        org.spongepowered.api.effect.particle.ParticleEffect spongeEffect = builder.build();
        org.spongepowered.api.world.World world = ((WorldWrapper)center.getWorld()).getSpongeWorld();

        world.spawnParticles(spongeEffect, new Vector3d(center.getX(), center.getY(), center.getZ()));
    }

    @Override
    public void displayColored(ParticleEffect effect, int red, int green, int blue, float speed, int amount, Location center) {
        ParticleType type = ParticleEffectUtil.toParticleType(effect);

        if (amount <= 0) amount = 1;

        org.spongepowered.api.effect.particle.ParticleEffect spongeEffect =
                org.spongepowered.api.effect.particle.ParticleEffect.builder()
                        .type(type)
                        .quantity(amount)
                        .offset(new Vector3d(0.0, 0.0, 0.0))
                        .velocity(new Vector3d(speed, speed, speed))
                        .option(ParticleOptions.COLOR, Color.ofRgb(red, green, blue))
                        .build();

        org.spongepowered.api.world.World world = ((WorldWrapper)center.getWorld()).getSpongeWorld();

        world.spawnParticles(spongeEffect, new Vector3d(center.getX(), center.getY(), center.getZ()));
    }
}
