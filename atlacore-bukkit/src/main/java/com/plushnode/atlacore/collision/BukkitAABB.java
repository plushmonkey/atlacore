package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.WorldWrapper;
import com.plushnode.atlacore.util.TypeUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BukkitAABB {
    private static Class<?> CraftWorld, CraftEntity, World, AxisAlignedBB, Block, BlockPosition, IBlockData, Entity, VoxelShape;
    private static Method getHandle, getEntityHandle, getType, getBlock, getBlockBoundingBox, getBoundingBox, getVoxelShape, isEmptyBounds;
    private static Field aField, bField, cField, dField, eField, fField;
    private static int serverVersion;

    private Vector min = null;
    private Vector max = null;

    static {
        serverVersion = 9;

        try {
            serverVersion = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().substring(23).split("_")[1]);
        } catch (Exception e) {

        }

        if (!setupReflection(serverVersion)) {
            Game.warn("ERROR: Failed to setup BukkitAABB reflection.");
        }
    }

    private BukkitAABB() {

    }

    public static AABB getBlockBounds(Block block) {
        Vector3D min = TypeUtil.adapt(getBlockMin(block));
        Vector3D max = TypeUtil.adapt(getBlockMax(block));

        return new AABB(min, max, new WorldWrapper(block.getWorld()));
    }

    public static AABB getEntityBounds(Entity entity) {
        Vector3D min = TypeUtil.adapt(getEntityMin(entity));
        Vector3D max = TypeUtil.adapt(getEntityMax(entity));

        if (min != null) {
            min = min.subtract(TypeUtil.adapt(entity.getLocation().toVector()));
        }

        if (max != null) {
            max = max.subtract(TypeUtil.adapt(entity.getLocation().toVector()));
        }

        return new AABB(min, max, new WorldWrapper(entity.getLocation().getWorld()));
    }

    private static Vector getEntityMin(Entity entity) {
        try {
            Object handle = getEntityHandle.invoke(CraftEntity.cast(entity));
            Object aabb = getBoundingBox.invoke(handle);

            if (aabb == null) return null;

            double a = (double)aField.get(aabb);
            double b = (double)bField.get(aabb);
            double c = (double)cField.get(aabb);

            return new Vector(a, b, c);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector getEntityMax(Entity entity) {
        try {
            Object handle = getEntityHandle.invoke(CraftEntity.cast(entity));
            Object aabb = getBoundingBox.invoke(handle);

            if (aabb == null) return null;

            double d = (double)dField.get(aabb);
            double e = (double)eField.get(aabb);
            double f = (double)fField.get(aabb);

            return new Vector(d, e, f);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector getBlockMin(Block block) {
        Object aabb = getAABB(block);
        if (aabb == null) return null;

        try {
            double a = (double)aField.get(aabb);
            double b = (double)bField.get(aabb);
            double c = (double)cField.get(aabb);

            return new Vector(a, b, c);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector getBlockMax(Block block) {
        Object aabb = getAABB(block);
        if (aabb == null) return null;

        try {
            double d = (double)dField.get(aabb);
            double e = (double)eField.get(aabb);
            double f = (double)fField.get(aabb);

            return new Vector(d, e, f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getAABB(Block block) {
        if (serverVersion < 13) {
            try {
                Constructor<?> bpConstructor = BlockPosition.getConstructor(int.class, int.class, int.class);
                Object bp = bpConstructor.newInstance(block.getX(), block.getY(), block.getZ());
                Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
                Object blockData = getType.invoke(world, BlockPosition.cast(bp));
                Object blockNative = getBlock.invoke(blockData);

                return getBlockBoundingBox.invoke(blockNative, IBlockData.cast(blockData), World.cast(world), BlockPosition.cast(bp));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Constructor<?> bpConstructor = BlockPosition.getConstructor(int.class, int.class, int.class);
                Object bp = bpConstructor.newInstance(block.getX(), block.getY(), block.getZ());
                Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
                Object blockData = getType.invoke(world, BlockPosition.cast(bp));
                Object blockNative = getBlock.invoke(blockData);
                Object voxelShape = getVoxelShape.invoke(blockNative, IBlockData.cast(blockData), World.cast(world), BlockPosition.cast(bp));
                Object emptyBounds = isEmptyBounds.invoke(voxelShape);

                if (emptyBounds != null && !(Boolean)emptyBounds) {
                    return getBlockBoundingBox.invoke(voxelShape);
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static boolean setupReflection(int serverVersion) {
        CraftWorld = getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
        CraftEntity = getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
        World = getNMSClass("net.minecraft.server.%s.World");
        AxisAlignedBB = getNMSClass("net.minecraft.server.%s.AxisAlignedBB");
        Block = getNMSClass("net.minecraft.server.%s.Block");
        BlockPosition = getNMSClass("net.minecraft.server.%s.BlockPosition");
        IBlockData = getNMSClass("net.minecraft.server.%s.IBlockData");
        Entity = getNMSClass("net.minecraft.server.%s.Entity");
        VoxelShape = getNMSClass("net.minecraft.server.%s.VoxelShape");
        Class<?> IBlockAccess = getNMSClass("net.minecraft.server.%s.IBlockAccess");

        if (serverVersion < 13) {
            try {
                getHandle = CraftWorld.getDeclaredMethod("getHandle");
                getEntityHandle = CraftEntity.getDeclaredMethod("getHandle");
                getType = World.getDeclaredMethod("getType", BlockPosition);
                getBlock = IBlockData.getDeclaredMethod("getBlock");
                getBlockBoundingBox = Block.getDeclaredMethod("a", IBlockData, serverVersion >= 11 ? IBlockAccess : World, BlockPosition);
                getBoundingBox = Entity.getDeclaredMethod("getBoundingBox");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            }
        } else {

            try {
                getHandle = CraftWorld.getDeclaredMethod("getHandle");
                getEntityHandle = CraftEntity.getDeclaredMethod("getHandle");
                getType = World.getDeclaredMethod("getType", BlockPosition);
                getBlock = IBlockData.getDeclaredMethod("getBlock");
                getVoxelShape = Block.getDeclaredMethod("a", IBlockData, serverVersion >= 11 ? IBlockAccess : World, BlockPosition);
                getBlockBoundingBox = VoxelShape.getDeclaredMethod("a");
                isEmptyBounds = VoxelShape.getDeclaredMethod("b");
                getBoundingBox = Entity.getDeclaredMethod("getBoundingBox");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            aField = AxisAlignedBB.getField("a");
            bField = AxisAlignedBB.getField("b");
            cField = AxisAlignedBB.getField("c");
            dField = AxisAlignedBB.getField("d");
            eField = AxisAlignedBB.getField("e");
            fField = AxisAlignedBB.getField("f");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }

        return true;
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