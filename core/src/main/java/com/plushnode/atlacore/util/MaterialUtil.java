package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.platform.block.data.Levelled;

import java.util.Arrays;
import java.util.List;

// TODO: All configurable
public final class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING,
            Material.COBWEB, Material.TALL_GRASS, Material.GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.WHEAT, Material.SNOW, Material.SUGAR_CANE, Material.VINE, Material.SUNFLOWER, Material.LILAC,
            Material.LARGE_FERN, Material.ROSE_BUSH, Material.PEONY, Material.CAVE_AIR, Material.VOID_AIR,
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN, Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
            Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
            Material.OAK_SIGN, Material.OAK_WALL_SIGN
    );

    private static final List<Material> EARTH_MATERIALS = Arrays.asList(
            Material.DIRT, Material.PODZOL, Material.COARSE_DIRT, Material.MYCELIUM, Material.GRASS_BLOCK,
            Material.STONE, Material.GRANITE, Material.POLISHED_GRANITE, Material.DIORITE, Material.POLISHED_DIORITE,
            Material.ANDESITE, Material.POLISHED_ANDESITE, Material.GRAVEL, Material.CLAY, Material.COAL_ORE,
            Material.COAL_BLOCK, Material.IRON_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE,
            Material.DIAMOND_ORE, Material.NETHERRACK, Material.NETHER_QUARTZ_ORE, Material.COBBLESTONE,
            Material.COBBLESTONE_STAIRS, Material.COBBLESTONE_SLAB, Material.STONE_BRICK_STAIRS, Material.DIRT_PATH,
            Material.STONE_BRICKS, Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.PINK_CONCRETE,
            Material.GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE, Material.CYAN_CONCRETE, Material.PURPLE_CONCRETE,
            Material.BLUE_CONCRETE, Material.BROWN_CONCRETE, Material.GREEN_CONCRETE, Material.RED_CONCRETE,
            Material.BLACK_CONCRETE, Material.MAGMA_BLOCK, Material.BRICK, Material.TERRACOTTA,
            Material.BLACK_TERRACOTTA, Material.BLUE_TERRACOTTA,Material.BROWN_TERRACOTTA, Material.CYAN_TERRACOTTA,
            Material.GRAY_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA, Material.LIME_TERRACOTTA, Material.MAGENTA_TERRACOTTA,
            Material.ORANGE_TERRACOTTA, Material.PINK_TERRACOTTA, Material.PURPLE_TERRACOTTA, Material.RED_TERRACOTTA,
            Material.WHITE_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.END_STONE, Material.GLOWSTONE,

            Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.SMOOTH_QUARTZ, Material.QUARTZ_BLOCK,
            Material.QUARTZ_STAIRS, Material.QUARTZ_SLAB, Material.GLASS, Material.WHITE_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.PINK_STAINED_GLASS,
            Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.CYAN_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.BROWN_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.BLACK_STAINED_GLASS, Material.OBSIDIAN,

            Material.SAND, Material.SANDSTONE, Material.SANDSTONE_STAIRS, Material.SANDSTONE_SLAB, Material.RED_SAND,
            Material.RED_SANDSTONE, Material.RED_SANDSTONE, Material.RED_SANDSTONE_STAIRS, Material.RED_SANDSTONE_SLAB,
            Material.GRAVEL, Material.WHITE_CONCRETE_POWDER, Material.ORANGE_CONCRETE_POWDER,
            Material.MAGENTA_CONCRETE_POWDER, Material.LIGHT_BLUE_CONCRETE_POWDER, Material.YELLOW_CONCRETE_POWDER,
            Material.LIME_CONCRETE_POWDER, Material.PINK_CONCRETE_POWDER, Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE_POWDER, Material.CYAN_CONCRETE_POWDER, Material.PURPLE_CONCRETE_POWDER,
            Material.BLUE_CONCRETE_POWDER, Material.BROWN_CONCRETE_POWDER, Material.GREEN_CONCRETE_POWDER,
            Material.RED_CONCRETE_POWDER, Material.BLACK_CONCRETE_POWDER, Material.SOUL_SAND
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

    private static final Material[] PLANT_MATERIALS = {
            Material.ACACIA_SAPLING, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.JUNGLE_SAPLING,
            Material.SPRUCE_SAPLING, Material.ACACIA_LEAVES, Material.BIRCH_LEAVES, Material.DARK_OAK_LEAVES,
            Material.JUNGLE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.DEAD_BUSH, Material.DANDELION,
            Material.POPPY, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.CACTUS, Material.PUMPKIN,
            Material.RED_MUSHROOM_BLOCK, Material.BROWN_MUSHROOM_BLOCK, Material.MELON, Material.VINE,
            Material.LILY_PAD, Material.SUNFLOWER, Material.LILAC, Material.LARGE_FERN, Material.ROSE_BUSH,
            Material.PEONY, Material.WHEAT, Material.GRASS, Material.TALL_GRASS, Material.FERN, Material.SUGAR_CANE,
            Material.PUMPKIN_STEM, Material.MELON_STEM, Material.BLUE_ORCHID, Material.OXEYE_DAISY, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP
    };

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

    public static boolean isPlant(Block block) {
        return isPlant(block.getType());
    }

    public static boolean isPlant(Material type) {
        return Arrays.asList(PLANT_MATERIALS).contains(type);
    }

    public static Material[] getPlantMaterials() {
        return PLANT_MATERIALS;
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

    public static boolean isAir(Block block) {
        return isAir(block.getType());
    }

    public static boolean isSourceBlock(Block block) {
        BlockData blockData = block.getBlockData();

        if (blockData instanceof Levelled) {
            return ((Levelled)blockData).getLevel() == 0;
        }

        return false;
    }
}
