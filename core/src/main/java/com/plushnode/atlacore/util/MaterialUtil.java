package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.collision.geometry.AABB;

import java.util.Arrays;
import java.util.List;

public final class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER, Material.LAVA,
            Material.STATIONARY_LAVA, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
            Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.CROPS, Material.SNOW, Material.SUGAR_CANE_BLOCK, Material.VINE, Material.DOUBLE_PLANT
    );

    private static final List<Material> EARTH_MATERIALS = Arrays.asList(
            Material.DIRT, Material.MYCEL, Material.GRASS, Material.STONE, Material.GRAVEL, Material.CLAY,
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE,
            Material.DIAMOND_ORE, Material.NETHERRACK, Material.QUARTZ_ORE, Material.COBBLESTONE, Material.STEP,
            Material.SAND, Material.SANDSTONE, Material.RED_SANDSTONE, Material.GRASS_PATH
    );

    // These are materials that must be attached to a block.
    private final static List<Material> ATTACH_MATERIALS = Arrays.asList( Material.SAPLING, Material.LONG_GRASS,
            Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM, Material.FIRE, Material.SNOW, Material.TORCH );

    // These are materials that fire can be placed on.
    private static final List<Material> IGNITABLE_MATERIALS = Arrays.asList( Material.BEDROCK, Material.BOOKSHELF,
            Material.BRICK, Material.CLAY, Material.CLAY_BRICK, Material.COAL_ORE, Material.COBBLESTONE,
            Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.DIRT, Material.ENDER_STONE,
            Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK, Material.GRAVEL, Material.GRASS,
            Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.LOG,
            Material.MOSSY_COBBLESTONE, Material.MYCEL, Material.NETHER_BRICK, Material.NETHERRACK, Material.OBSIDIAN,
            Material.REDSTONE_ORE, Material.SAND, Material.SANDSTONE, Material.SMOOTH_BRICK,Material.STONE,
            Material.SOUL_SAND,Material.WOOD, Material.WOOL, Material.LEAVES, Material.LEAVES_2, Material.MELON_BLOCK,
            Material.PUMPKIN, Material.JACK_O_LANTERN, Material.NOTE_BLOCK, Material.GLOWSTONE, Material.IRON_BLOCK,
            Material.DISPENSER, Material.SPONGE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_BLOCK,
            Material.WORKBENCH, Material.HAY_BLOCK, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON,
            Material.EMERALD_ORE, Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK, Material.QUARTZ_BLOCK,
            Material.QUARTZ_ORE, Material.STAINED_CLAY, Material.HARD_CLAY, Material.GRASS_PATH
    );

    private MaterialUtil() {

    }

    public static boolean isTransparent(Block block) {
        return TRANSPARENT_MATERIALS.contains(block.getType());
    }

    public static boolean isSolid(Block block) {
        AABB blockBounds = block.getBounds();

        // The block bounding box will have width if it's solid.
        if (blockBounds.min() == null || blockBounds.max() == null) return false;

        return blockBounds.min().distanceSq(blockBounds.max()) > 0;
    }

    public static boolean isIgnitable(Block block) {
        Material type = block.getType();
        if (type != Material.AIR && !ATTACH_MATERIALS.contains(type)) {
            return false;
        }

        // The current block is either air or can be overwritten.
        // The below block still needs to be checked because it could be a torch against a wall.

        Block belowBlock = block.getRelative(BlockFace.DOWN);
        return IGNITABLE_MATERIALS.contains(belowBlock.getType());
    }

    public static boolean isEarthbendable(Block block) {
        return EARTH_MATERIALS.contains(block.getType());
    }

    public static boolean isLava(Block block) {
        Material type = block.getType();
        return type == Material.LAVA || type == Material.STATIONARY_LAVA;
    }
}
