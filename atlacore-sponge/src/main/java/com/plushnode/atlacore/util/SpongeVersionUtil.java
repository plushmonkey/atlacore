package com.plushnode.atlacore.util;

import com.plushnode.atlacore.AtlaPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Handle certain things that changed between Sponge API versions
public final class SpongeVersionUtil {
    private static Class<?> eventContextClass;
    private static Method setBlockTypeMethod;
    private static Method setCauseSource;
    private static Method buildCause;

    static {
        try {
            eventContextClass = Class.forName("org.spongepowered.api.event.cause.EventContext");
        } catch (ClassNotFoundException e) {

        }

        if (eventContextClass == null) {
            try {
                setBlockTypeMethod = Location.class.getMethod("setBlockType", BlockType.class, Cause.class);
                setCauseSource = Cause.class.getMethod("source", Object.class);
                buildCause = Cause.Builder.class.getMethod("build");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private SpongeVersionUtil() {

    }

    public static boolean setBlockType(Location<World> location, BlockType type) {
        if (eventContextClass == null) {
            PluginContainer container = Sponge.getPluginManager().fromInstance(AtlaPlugin.plugin).orElse(null);
            try {
                Cause.Builder builder = (Cause.Builder)setCauseSource.invoke(null, container);
                Cause cause = (Cause)buildCause.invoke(builder);
                return (boolean)setBlockTypeMethod.invoke(location, type, cause);
            } catch (IllegalAccessException|InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        location.setBlockType(type);

        return true;
    }
}
