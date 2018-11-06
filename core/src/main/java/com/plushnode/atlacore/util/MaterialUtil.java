package com.plushnode.atlacore.util;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.collision.geometry.AABB;

import java.util.Arrays;
import java.util.List;

public final class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.WATER,
            Material.LAVA, Material.COBWEB, Material.TALL_GRASS, Material.GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.DANDELION, Material.DANDELION_YELLOW, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.WHEAT, Material.SNOW, Material.SUGAR_CANE, Material.VINE, Material.SUNFLOWER, Material.LILAC,
            Material.LARGE_FERN, Material.ROSE_BUSH, Material.PEONY, Material.CAVE_AIR, Material.VOID_AIR
    );

    private static final List<Material> EARTH_MATERIALS = Arrays.asList(
            Material.DIRT, Material.MYCELIUM, Material.GRASS_BLOCK, Material.STONE, Material.GRANITE,
            Material.POLISHED_GRANITE, Material.DIORITE, Material.POLISHED_DIORITE, Material.ANDESITE,
            Material.POLISHED_ANDESITE, Material.GRAVEL, Material.CLAY, Material.COAL_ORE, Material.IRON_ORE,
            Material.GOLD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE, Material.NETHERRACK,
            Material.NETHER_QUARTZ_ORE, Material.COBBLESTONE, Material.COBBLESTONE_STAIRS, Material.STONE_BRICK_STAIRS,
            Material.STONE_BRICKS, Material.SAND, Material.SANDSTONE, Material.RED_SAND, Material.RED_SANDSTONE,
            Material.GRASS_PATH
    );

    // These are materials that must be attached to a block.
    private final static List<Material> ATTACH_MATERIALS = Arrays.asList(
            Material.ACACIA_SAPLING, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.JUNGLE_SAPLING,
            Material.SPRUCE_SAPLING, Material.GRASS, Material.POPPY, Material.DANDELION, Material.DEAD_BUSH,
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE, Material.SNOW, Material.TORCH,
            Material.SUNFLOWER, Material.LILAC, Material.LARGE_FERN, Material.FERN, Material.ROSE_BUSH, Material.PEONY
    );

    // These are materials that fire can be placed on.
    private static final List<Material> IGNITABLE_MATERIALS = Arrays.asList( Material.BEDROCK, Material.BOOKSHELF,
            Material.BRICK, Material.CLAY, Material.BRICKS, Material.COAL_ORE, Material.COBBLESTONE,
            Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.DIRT, Material.END_STONE, Material.REDSTONE_ORE,
            Material.GOLD_BLOCK, Material.GRAVEL, Material.GRASS_BLOCK, Material.BROWN_MUSHROOM_BLOCK,
            Material.RED_MUSHROOM_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.OAK_LOG,
            Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.SPRUCE_LOG,
            Material.MOSSY_COBBLESTONE, Material.MYCELIUM, Material.NETHER_BRICK, Material.NETHERRACK,
            Material.OBSIDIAN, Material.SAND, Material.SANDSTONE, Material.STONE_BRICKS, Material.STONE,
            Material.SOUL_SAND, Material.ACACIA_WOOD, Material.BIRCH_WOOD, Material.DARK_OAK_WOOD, Material.JUNGLE_WOOD,
            Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BLACK_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL,
            Material.CYAN_WOOL, Material.GRAY_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL, Material.LIGHT_GRAY_WOOL,
            Material.LIME_WOOL, Material.MAGENTA_WOOL, Material.ORANGE_WOOL, Material.PINK_WOOL, Material.PURPLE_WOOL,
            Material.RED_WOOL, Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ACACIA_LEAVES, Material.BIRCH_LEAVES,
            Material.DARK_OAK_LEAVES, Material.JUNGLE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES,
            Material.MELON, Material.PUMPKIN, Material.JACK_O_LANTERN, Material.NOTE_BLOCK, Material.GLOWSTONE,
            Material.IRON_BLOCK, Material.DISPENSER, Material.SPONGE, Material.IRON_ORE, Material.GOLD_ORE,
            Material.COAL_BLOCK, Material.CRAFTING_TABLE, Material.HAY_BLOCK, Material.REDSTONE_LAMP,
            Material.EMERALD_ORE, Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK, Material.QUARTZ_BLOCK,
            Material.NETHER_QUARTZ_ORE, Material.TERRACOTTA, Material.BLACK_TERRACOTTA, Material.BLUE_TERRACOTTA,
            Material.BROWN_TERRACOTTA, Material.CYAN_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.GREEN_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA, Material.LIME_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.PINK_TERRACOTTA,
            Material.PURPLE_TERRACOTTA, Material.RED_TERRACOTTA, Material.WHITE_TERRACOTTA, Material.YELLOW_TERRACOTTA
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

    public static boolean isEarthbendable(Material type) {
        return EARTH_MATERIALS.contains(type);
    }

    public static boolean isLava(Block block) {
        Material type = block.getType();
        return type == Material.LAVA;
    }

    // Turns a falling-type block into a fully solid block so it can be moved.
    // Returns true if the block type was changed.
    public static boolean resolveSolidEarth(Block block) {
        Material type = block.getType();

        // TODO: implement the rest
        if (type == Material.SAND) {
            new TempBlock(block, Material.SANDSTONE);
            return true;
        } else if (type == Material.RED_SAND) {
            new TempBlock(block, Material.RED_SANDSTONE);
            return true;
        } else if (type == Material.GRAVEL) {
            new TempBlock(block, Material.COBBLESTONE);
            return true;
        }

        return false;
    }

    // Finds a suitable solid block type to replace a falling-type block with.
    public static Material getSolidEarthType(Material type) {
        // TODO: implement the rest
        if (type == Material.SAND) {
            return Material.SANDSTONE;
        } else if (type == Material.RED_SAND) {
            return Material.RED_SANDSTONE;
        } else if (type == Material.GRAVEL) {
            return Material.COBBLESTONE;
        }

        return type;
    }

    public static boolean isAir(Material material) {
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
    }
}
