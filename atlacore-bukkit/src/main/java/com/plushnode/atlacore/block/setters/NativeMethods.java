package com.plushnode.atlacore.block.setters;

import com.plushnode.atlacore.game.Game;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeMethods {
    private static Class<?> CraftWorld, World, BlockPosition, NMSBlock, Chunk, ChunkSection, IBlockData;
    private static Constructor<?> blockPositionConstructor;
    private static Method getHandle, getChunkAt, getByCombinedId, a, notify, setType, getType;
    private static Field sections;
    private static boolean blockDataNotify = false;
    public static boolean enabled = false;

    static {
        try {
            setupReflection();
            enabled = true;
        } catch (NoSuchMethodException e) {
            if (Game.plugin != null) {
              Game.warn("Failed to setup reflection for fast block setting.");
            }
            //e.printStackTrace();
        }
    }

    public static boolean setBlockFast(Block block, int typeId, int data) {
        if (!enabled) return false;

        Location location = block.getLocation();

        try {
            int x = (int)((long)location.getX() >> 4);
            int z = (int)((long)location.getZ() >> 4);

            Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
            Object chunk = getChunkAt.invoke(world, x, z);
            Object blockPosition = blockPositionConstructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            int fullData = typeId + (data << 12);
            Object blockData = getByCombinedId.invoke(null, fullData);

            Object[] chunkSections = (Object[])sections.get(chunk);

            if (chunkSections == null) {
                return false;
            }

            Object chunkSection = chunkSections[location.getBlockY() >> 4];

            if (chunkSection == null) {
                return false;
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
        World = getNMSClass("net.minecraft.server.%s.World");
        BlockPosition = getNMSClass("net.minecraft.server.%s.BlockPosition");
        NMSBlock = getNMSClass("net.minecraft.server.%s.Block");
        IBlockData = getNMSClass("net.minecraft.server.%s.IBlockData");
        Chunk = getNMSClass("net.minecraft.server.%s.Chunk");
        ChunkSection = getNMSClass("net.minecraft.server.%s.ChunkSection");

        blockPositionConstructor = BlockPosition.getConstructor(int.class, int.class, int.class);

        getHandle = CraftWorld.getDeclaredMethod("getHandle");
        getChunkAt = World.getDeclaredMethod("getChunkAt", int.class, int.class);
        getByCombinedId = NMSBlock.getDeclaredMethod("getByCombinedId", int.class);
        a = Chunk.getDeclaredMethod("a", BlockPosition, IBlockData);
        setType = ChunkSection.getDeclaredMethod("setType", int.class, int.class, int.class, IBlockData);
        getType = ChunkSection.getDeclaredMethod("getType", int.class, int.class, int.class);

        try {
            sections = Chunk.getDeclaredField("sections");
            sections.setAccessible(true);
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
