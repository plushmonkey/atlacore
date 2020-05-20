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
    private BukkitAABB() {

    }

    public static AABB getBlockBounds(Block block) {
        Vector worldMin = block.getBoundingBox().getMin();
        Vector localMin = worldMin.clone().subtract(block.getLocation().toVector());
        Vector worldMax = block.getBoundingBox().getMax();
        Vector localMax = worldMax.clone().subtract(block.getLocation().toVector());

        Vector3D min = TypeUtil.adapt(localMin);
        Vector3D max = TypeUtil.adapt(localMax);

        if (block.isPassable()) {
            min = null;
            max = null;
        }

        return new AABB(min, max, new WorldWrapper(block.getWorld()));
    }

    public static AABB getEntityBounds(Entity entity) {
        Vector worldMin = entity.getBoundingBox().getMin();
        Vector localMin = worldMin.clone().subtract(entity.getLocation().toVector());
        Vector worldMax = entity.getBoundingBox().getMax();
        Vector localMax = worldMax.clone().subtract(entity.getLocation().toVector());

        Vector3D min = TypeUtil.adapt(localMin);
        Vector3D max = TypeUtil.adapt(localMax);

        return new AABB(min, max, new WorldWrapper(entity.getLocation().getWorld()));
    }

}
