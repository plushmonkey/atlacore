package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeMethods {
    public static boolean enabled = false;

    private static Class<?> CraftWorld;
    private static Constructor<?> blockPositionConstructor, chunkSectionConstructor;
    private static Method getHandle, getChunkAt, notify, setType, getType, getFlag, getBlockDataState;
    private static Field sections, worldProviderField;
    private static boolean blockDataNotify = false, chunkSectionFlag = false;

    static {
        try {
            setupReflection();
            enabled = true;
        } catch (NoSuchMethodException e) {
            if (Game.plugin != null) {
              Game.warn("Failed to setup reflection for fast block setting.");
            }
            e.printStackTrace();
        }
    }

    public static boolean setBlockFast(Block block, BlockData data) {
        if (!enabled) return false;

        if (block.getLocation().getY() < 0 || block.getLocation().getY() > 255) return true;
        if (block.getBlockData().equals(data)) return true;

        Location location = block.getLocation();

        try {
            int x = (int)((long)location.getX() >> 4);
            int z = (int)((long)location.getZ() >> 4);

            Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
            Object chunk = getChunkAt.invoke(world, x, z);
            Object blockPosition = blockPositionConstructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Object blockData = getBlockDataState.invoke(data);

            Object[] chunkSections = (Object[])sections.get(chunk);

            if (chunkSections == null) {
                return false;
            }

            Object chunkSection = chunkSections[location.getBlockY() >> 4];

            if (chunkSection == null) {
                Object worldProvider = worldProviderField.get(world);
                boolean flag = (Boolean)getFlag.invoke(worldProvider);

                int yPos = (location.getBlockY() >> 4) << 4;

                if (chunkSectionFlag) {
                    chunkSection = chunkSectionConstructor.newInstance(yPos, flag);
                } else {
                    chunkSection = chunkSectionConstructor.newInstance(yPos);
                }

                chunkSections[location.getBlockY() >> 4] = chunkSection;
            }

            int sectionX = location.getBlockX() & 15;
            int sectionY = location.getBlockY() & 15;
            int sectionZ = location.getBlockZ() & 15;

            Object oldBlockData = getType.invoke(chunkSection, sectionX, sectionY, sectionZ);

            setType.invoke(chunkSection, sectionX, sectionY, sectionZ, blockData);

            if (blockDataNotify) {
                int flag = 0;
                notify.invoke(world, blockPosition, oldBlockData, blockData, flag);
            } else {
                notify.invoke(world, blockPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void setupReflection() throws NoSuchMethodException {
        CraftWorld = getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
        Class<?> World = getNMSClass("net.minecraft.server.%s.World");
        Class<?> BlockPosition = getNMSClass("net.minecraft.server.%s.BlockPosition");
        Class<?> IBlockData = getNMSClass("net.minecraft.server.%s.IBlockData");
        Class<?> Chunk = getNMSClass("net.minecraft.server.%s.Chunk");
        Class<?> ChunkSection = getNMSClass("net.minecraft.server.%s.ChunkSection");

        blockPositionConstructor = BlockPosition.getConstructor(int.class, int.class, int.class);

        try {
            chunkSectionConstructor = ChunkSection.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            chunkSectionConstructor = ChunkSection.getConstructor(int.class, boolean.class);
            chunkSectionFlag = true;
        }

        getHandle = CraftWorld.getDeclaredMethod("getHandle");
        getChunkAt = World.getDeclaredMethod("getChunkAt", int.class, int.class);
        setType = ChunkSection.getDeclaredMethod("setType", int.class, int.class, int.class, IBlockData);
        getType = ChunkSection.getDeclaredMethod("getType", int.class, int.class, int.class);

        Class<?> WorldProvider = getNMSClass("net.minecraft.server.%s.WorldProvider");
        if (WorldProvider != null) {
            getFlag = WorldProvider.getDeclaredMethod("g");
        }

        try {
            sections = Chunk.getDeclaredField("sections");
            sections.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            worldProviderField = World.getDeclaredField("worldProvider");
            worldProviderField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            notify = World.getDeclaredMethod("notify", BlockPosition);

        } catch (NoSuchMethodException e) {
            notify = null;
        }

        if (notify == null) {
            notify = World.getDeclaredMethod("notify", BlockPosition, IBlockData, IBlockData, int.class);
            blockDataNotify = true;
        }

        Class<?> craftBlockDataClazz = getNMSClass("org.bukkit.craftbukkit.%s.block.data.CraftBlockData");
        getBlockDataState = craftBlockDataClazz.getDeclaredMethod("getState");
    }

    private static Class<?> getNMSClass(String nmsClass) {
        String version = null;

        Pattern pattern = Pattern.compile("net\\.minecraft\\.(?:server)?\\.(v(?:\\d+_)+R\\d)");
        for (Package p : Package.getPackages()) {
            String name = p.getName();
            Matcher m = pattern.matcher(name);
            if (m.matches()) {
                version = m.group(1);
            }
        }

        if (version == null) return null;

        try {
            return Class.forName(String.format(nmsClass, version));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
