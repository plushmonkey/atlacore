package com.plushnode.atlacore.material;

import com.plushnode.atlacore.platform.block.Material;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SpongeMaterialUtil {
    private static Method toMaterialMethod, toBlockTypeMethod;

    static {
        MinecraftVersion version = Sponge.getPlatform().getMinecraftVersion();
        String major = version.getName().substring(0, version.getName().lastIndexOf('.')).replace('.', '_');

        String targetClassPath = SpongeMaterialUtil.class.getPackage().getName() + ".internal.SpongeMaterialUtil_" + major;

        try {
            Class<?> implClazz = Class.forName(targetClassPath);

            toMaterialMethod = implClazz.getDeclaredMethod("toMaterial", BlockType.class);
            toBlockTypeMethod = implClazz.getDeclaredMethod("toBlockType", Material.class);

        } catch (ClassNotFoundException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private SpongeMaterialUtil() {

    }

    public static Material toMaterial(BlockType type) {
        try {
            return (Material)toMaterialMethod.invoke(null, type);
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }

        return Material.AIR;
    }

    public static BlockType toBlockType(Material material) {
        try {
            return (BlockType)toBlockTypeMethod.invoke(null, material);
        } catch (IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }

        return BlockTypes.AIR;
    }

    public static Material toMaterial(ItemType type) {
        return Material.getFromId(type.getId());
    }

    public static ItemType toItemType(Material material) {
        String id = "minecraft:" + material.toString().toLowerCase();
        Optional<ItemType> result = Sponge.getGame().getRegistry().getType(ItemType.class, id);

        if (result.isPresent()) {
            return result.get();
        }

        Optional<BlockType> blockResult = Sponge.getGame().getRegistry().getType(BlockType.class, id);

        if (blockResult.isPresent()) {
            return blockResult.get().getItem().orElse(null);
        }

        return null;
    }
}
