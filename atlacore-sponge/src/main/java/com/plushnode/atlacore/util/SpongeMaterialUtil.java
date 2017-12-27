package com.plushnode.atlacore.util;

import com.plushnode.atlacore.block.Material;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SpongeMaterialUtil {
    private static Map<Material, BlockType> blockTypes = new HashMap<>();

    private SpongeMaterialUtil() {
    }

    static {
        // Regex find/replace for creating this.
        // (?<Type>.+)\(.+\),
        // blockTypes.put\(Material.$+{Type}, BlockTypes.$+{Type}\);

        blockTypes.put(Material.AIR, BlockTypes.AIR);
        blockTypes.put(Material.STONE, BlockTypes.STONE);
        blockTypes.put(Material.GRASS, BlockTypes.GRASS);
        blockTypes.put(Material.DIRT, BlockTypes.DIRT);
        blockTypes.put(Material.COBBLESTONE, BlockTypes.COBBLESTONE);
        blockTypes.put(Material.WOOD, BlockTypes.PLANKS);
        blockTypes.put(Material.SAPLING, BlockTypes.SAPLING);
        blockTypes.put(Material.BEDROCK, BlockTypes.BEDROCK);
        blockTypes.put(Material.WATER, BlockTypes.FLOWING_WATER);
        blockTypes.put(Material.STATIONARY_WATER, BlockTypes.WATER);
        blockTypes.put(Material.LAVA, BlockTypes.FLOWING_LAVA);
        blockTypes.put(Material.STATIONARY_LAVA, BlockTypes.LAVA);
        blockTypes.put(Material.SAND, BlockTypes.SAND);
        blockTypes.put(Material.GRAVEL, BlockTypes.GRAVEL);
        blockTypes.put(Material.GOLD_ORE, BlockTypes.GOLD_ORE);
        blockTypes.put(Material.IRON_ORE, BlockTypes.IRON_ORE);
        blockTypes.put(Material.COAL_ORE, BlockTypes.COAL_ORE);
        blockTypes.put(Material.LOG, BlockTypes.LOG);
        blockTypes.put(Material.LEAVES, BlockTypes.LEAVES);
        blockTypes.put(Material.SPONGE, BlockTypes.SPONGE);
        blockTypes.put(Material.GLASS, BlockTypes.GLASS);
        blockTypes.put(Material.LAPIS_ORE, BlockTypes.LAPIS_ORE);
        blockTypes.put(Material.LAPIS_BLOCK, BlockTypes.LAPIS_BLOCK);
        blockTypes.put(Material.DISPENSER, BlockTypes.DISPENSER);
        blockTypes.put(Material.SANDSTONE, BlockTypes.SANDSTONE);
        blockTypes.put(Material.NOTE_BLOCK, BlockTypes.NOTEBLOCK);
        blockTypes.put(Material.BED_BLOCK, BlockTypes.BED);
        blockTypes.put(Material.POWERED_RAIL, BlockTypes.GOLDEN_RAIL);
        blockTypes.put(Material.DETECTOR_RAIL, BlockTypes.DETECTOR_RAIL);
        blockTypes.put(Material.PISTON_STICKY_BASE, BlockTypes.STICKY_PISTON);
        blockTypes.put(Material.WEB, BlockTypes.WEB);
        blockTypes.put(Material.LONG_GRASS, BlockTypes.TALLGRASS);
        blockTypes.put(Material.DEAD_BUSH, BlockTypes.DEADBUSH);
        blockTypes.put(Material.PISTON_BASE, BlockTypes.PISTON);
        blockTypes.put(Material.PISTON_EXTENSION, BlockTypes.PISTON_EXTENSION);
        blockTypes.put(Material.WOOL, BlockTypes.WOOL);
        blockTypes.put(Material.PISTON_MOVING_PIECE, BlockTypes.PISTON_HEAD);
        blockTypes.put(Material.YELLOW_FLOWER, BlockTypes.YELLOW_FLOWER);
        blockTypes.put(Material.RED_ROSE, BlockTypes.RED_FLOWER);
        blockTypes.put(Material.BROWN_MUSHROOM, BlockTypes.BROWN_MUSHROOM);
        blockTypes.put(Material.RED_MUSHROOM, BlockTypes.RED_MUSHROOM);
        blockTypes.put(Material.GOLD_BLOCK, BlockTypes.GOLD_BLOCK);
        blockTypes.put(Material.IRON_BLOCK, BlockTypes.IRON_BLOCK);
        blockTypes.put(Material.DOUBLE_STEP, BlockTypes.DOUBLE_STONE_SLAB);
        blockTypes.put(Material.STEP, BlockTypes.STONE_SLAB);
        blockTypes.put(Material.BRICK, BlockTypes.BRICK_BLOCK);
        blockTypes.put(Material.TNT, BlockTypes.TNT);
        blockTypes.put(Material.BOOKSHELF, BlockTypes.BOOKSHELF);
        blockTypes.put(Material.MOSSY_COBBLESTONE, BlockTypes.MOSSY_COBBLESTONE);
        blockTypes.put(Material.OBSIDIAN, BlockTypes.OBSIDIAN);
        blockTypes.put(Material.TORCH, BlockTypes.TORCH);
        blockTypes.put(Material.FIRE, BlockTypes.FIRE);
        blockTypes.put(Material.MOB_SPAWNER, BlockTypes.MOB_SPAWNER);
        blockTypes.put(Material.WOOD_STAIRS, BlockTypes.OAK_STAIRS);
        blockTypes.put(Material.CHEST, BlockTypes.CHEST);
        blockTypes.put(Material.REDSTONE_WIRE, BlockTypes.REDSTONE_WIRE);
        blockTypes.put(Material.DIAMOND_ORE, BlockTypes.DIAMOND_ORE);
        blockTypes.put(Material.DIAMOND_BLOCK, BlockTypes.DIAMOND_BLOCK);
        blockTypes.put(Material.WORKBENCH, BlockTypes.CRAFTING_TABLE);
        blockTypes.put(Material.CROPS, BlockTypes.WHEAT);
        blockTypes.put(Material.SOIL, BlockTypes.FARMLAND);
        blockTypes.put(Material.FURNACE, BlockTypes.FURNACE);
        blockTypes.put(Material.BURNING_FURNACE, BlockTypes.LIT_FURNACE);
        blockTypes.put(Material.SIGN_POST, BlockTypes.STANDING_SIGN);
        blockTypes.put(Material.WOODEN_DOOR, BlockTypes.WOODEN_DOOR);
        blockTypes.put(Material.LADDER, BlockTypes.LADDER);
        blockTypes.put(Material.RAILS, BlockTypes.RAIL);
        blockTypes.put(Material.COBBLESTONE_STAIRS, BlockTypes.STONE_STAIRS);
        blockTypes.put(Material.WALL_SIGN, BlockTypes.WALL_SIGN);
        blockTypes.put(Material.LEVER, BlockTypes.LEVER);
        blockTypes.put(Material.STONE_PLATE, BlockTypes.STONE_PRESSURE_PLATE);
        blockTypes.put(Material.IRON_DOOR_BLOCK, BlockTypes.IRON_DOOR);
        blockTypes.put(Material.WOOD_PLATE, BlockTypes.WOODEN_PRESSURE_PLATE);
        blockTypes.put(Material.REDSTONE_ORE, BlockTypes.REDSTONE_ORE);
        blockTypes.put(Material.GLOWING_REDSTONE_ORE, BlockTypes.LIT_REDSTONE_ORE);
        blockTypes.put(Material.REDSTONE_TORCH_OFF, BlockTypes.UNLIT_REDSTONE_TORCH);
        blockTypes.put(Material.REDSTONE_TORCH_ON, BlockTypes.REDSTONE_TORCH);
        blockTypes.put(Material.STONE_BUTTON, BlockTypes.STONE_BUTTON);
        blockTypes.put(Material.SNOW, BlockTypes.SNOW);
        blockTypes.put(Material.ICE, BlockTypes.ICE);
        blockTypes.put(Material.SNOW_BLOCK, BlockTypes.SNOW);
        blockTypes.put(Material.CACTUS, BlockTypes.CACTUS);
        blockTypes.put(Material.CLAY, BlockTypes.CLAY);
        blockTypes.put(Material.SUGAR_CANE_BLOCK, BlockTypes.REEDS);
        blockTypes.put(Material.JUKEBOX, BlockTypes.JUKEBOX);
        blockTypes.put(Material.FENCE, BlockTypes.FENCE);
        blockTypes.put(Material.PUMPKIN, BlockTypes.PUMPKIN);
        blockTypes.put(Material.NETHERRACK, BlockTypes.NETHERRACK);
        blockTypes.put(Material.SOUL_SAND, BlockTypes.SOUL_SAND);
        blockTypes.put(Material.GLOWSTONE, BlockTypes.GLOWSTONE);
        blockTypes.put(Material.PORTAL, BlockTypes.PORTAL);
        blockTypes.put(Material.JACK_O_LANTERN, BlockTypes.LIT_PUMPKIN);
        blockTypes.put(Material.CAKE_BLOCK, BlockTypes.CAKE);
        blockTypes.put(Material.DIODE_BLOCK_OFF, BlockTypes.UNPOWERED_REPEATER);
        blockTypes.put(Material.DIODE_BLOCK_ON, BlockTypes.POWERED_REPEATER);
        blockTypes.put(Material.STAINED_GLASS, BlockTypes.STAINED_GLASS);
        blockTypes.put(Material.TRAP_DOOR, BlockTypes.TRAPDOOR);
        blockTypes.put(Material.MONSTER_EGGS, BlockTypes.MONSTER_EGG);
        blockTypes.put(Material.SMOOTH_BRICK, BlockTypes.STONEBRICK);
        blockTypes.put(Material.HUGE_MUSHROOM_1, BlockTypes.BROWN_MUSHROOM_BLOCK);
        blockTypes.put(Material.HUGE_MUSHROOM_2, BlockTypes.RED_MUSHROOM_BLOCK);
        blockTypes.put(Material.IRON_FENCE, BlockTypes.IRON_BARS);
        blockTypes.put(Material.THIN_GLASS, BlockTypes.GLASS_PANE);
        blockTypes.put(Material.MELON_BLOCK, BlockTypes.MELON_BLOCK);
        blockTypes.put(Material.PUMPKIN_STEM, BlockTypes.PUMPKIN_STEM);
        blockTypes.put(Material.MELON_STEM, BlockTypes.MELON_STEM);
        blockTypes.put(Material.VINE, BlockTypes.VINE);
        blockTypes.put(Material.FENCE_GATE, BlockTypes.FENCE_GATE);
        blockTypes.put(Material.BRICK_STAIRS, BlockTypes.BRICK_STAIRS);
        blockTypes.put(Material.SMOOTH_STAIRS, BlockTypes.STONE_BRICK_STAIRS);
        blockTypes.put(Material.MYCEL, BlockTypes.MYCELIUM);
        blockTypes.put(Material.WATER_LILY, BlockTypes.WATERLILY);
        blockTypes.put(Material.NETHER_BRICK, BlockTypes.NETHER_BRICK);
        blockTypes.put(Material.NETHER_FENCE, BlockTypes.NETHER_BRICK_FENCE);
        blockTypes.put(Material.NETHER_BRICK_STAIRS, BlockTypes.NETHER_BRICK_STAIRS);
        blockTypes.put(Material.NETHER_WARTS, BlockTypes.NETHER_WART);
        blockTypes.put(Material.ENCHANTMENT_TABLE, BlockTypes.ENCHANTING_TABLE);
        blockTypes.put(Material.BREWING_STAND, BlockTypes.BREWING_STAND);
        blockTypes.put(Material.CAULDRON, BlockTypes.CAULDRON);
        blockTypes.put(Material.ENDER_PORTAL, BlockTypes.END_PORTAL);
        blockTypes.put(Material.ENDER_PORTAL_FRAME, BlockTypes.END_PORTAL_FRAME);
        blockTypes.put(Material.ENDER_STONE, BlockTypes.END_STONE);
        blockTypes.put(Material.DRAGON_EGG, BlockTypes.DRAGON_EGG);
        blockTypes.put(Material.REDSTONE_LAMP_OFF, BlockTypes.REDSTONE_LAMP);
        blockTypes.put(Material.REDSTONE_LAMP_ON, BlockTypes.LIT_REDSTONE_LAMP);
        blockTypes.put(Material.WOOD_DOUBLE_STEP, BlockTypes.DOUBLE_WOODEN_SLAB);
        blockTypes.put(Material.WOOD_STEP, BlockTypes.WOODEN_SLAB);
        blockTypes.put(Material.COCOA, BlockTypes.COCOA);
        blockTypes.put(Material.SANDSTONE_STAIRS, BlockTypes.SANDSTONE_STAIRS);
        blockTypes.put(Material.EMERALD_ORE, BlockTypes.EMERALD_ORE);
        blockTypes.put(Material.ENDER_CHEST, BlockTypes.ENDER_CHEST);
        blockTypes.put(Material.TRIPWIRE_HOOK, BlockTypes.TRIPWIRE_HOOK);
        blockTypes.put(Material.TRIPWIRE, BlockTypes.TRIPWIRE);
        blockTypes.put(Material.EMERALD_BLOCK, BlockTypes.EMERALD_BLOCK);
        blockTypes.put(Material.SPRUCE_WOOD_STAIRS, BlockTypes.SPRUCE_STAIRS);
        blockTypes.put(Material.BIRCH_WOOD_STAIRS, BlockTypes.BIRCH_STAIRS);
        blockTypes.put(Material.JUNGLE_WOOD_STAIRS, BlockTypes.JUNGLE_STAIRS);
        blockTypes.put(Material.COMMAND, BlockTypes.COMMAND_BLOCK);
        blockTypes.put(Material.BEACON, BlockTypes.BEACON);
        blockTypes.put(Material.COBBLE_WALL, BlockTypes.COBBLESTONE_WALL);
        blockTypes.put(Material.FLOWER_POT, BlockTypes.FLOWER_POT);
        blockTypes.put(Material.CARROT, BlockTypes.CARROTS);
        blockTypes.put(Material.POTATO, BlockTypes.POTATOES);
        blockTypes.put(Material.WOOD_BUTTON, BlockTypes.WOODEN_BUTTON);
        blockTypes.put(Material.SKULL, BlockTypes.SKULL);
        blockTypes.put(Material.ANVIL, BlockTypes.ANVIL);
        blockTypes.put(Material.TRAPPED_CHEST, BlockTypes.TRAPPED_CHEST);
        blockTypes.put(Material.GOLD_PLATE, BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE);
        blockTypes.put(Material.IRON_PLATE, BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE);
        blockTypes.put(Material.REDSTONE_COMPARATOR_OFF, BlockTypes.UNPOWERED_COMPARATOR);
        blockTypes.put(Material.REDSTONE_COMPARATOR_ON, BlockTypes.POWERED_COMPARATOR);
        blockTypes.put(Material.DAYLIGHT_DETECTOR, BlockTypes.DAYLIGHT_DETECTOR);
        blockTypes.put(Material.REDSTONE_BLOCK, BlockTypes.REDSTONE_BLOCK);
        blockTypes.put(Material.QUARTZ_ORE, BlockTypes.QUARTZ_ORE);
        blockTypes.put(Material.HOPPER, BlockTypes.HOPPER);
        blockTypes.put(Material.QUARTZ_BLOCK, BlockTypes.QUARTZ_BLOCK);
        blockTypes.put(Material.QUARTZ_STAIRS, BlockTypes.QUARTZ_STAIRS);
        blockTypes.put(Material.ACTIVATOR_RAIL, BlockTypes.ACTIVATOR_RAIL);
        blockTypes.put(Material.DROPPER, BlockTypes.DROPPER);
        blockTypes.put(Material.STAINED_CLAY, BlockTypes.STAINED_HARDENED_CLAY);
        blockTypes.put(Material.STAINED_GLASS_PANE, BlockTypes.STAINED_GLASS_PANE);
        blockTypes.put(Material.LEAVES_2, BlockTypes.LEAVES2);
        blockTypes.put(Material.LOG_2, BlockTypes.LOG2);
        blockTypes.put(Material.ACACIA_STAIRS, BlockTypes.ACACIA_STAIRS);
        blockTypes.put(Material.DARK_OAK_STAIRS, BlockTypes.DARK_OAK_STAIRS);
        blockTypes.put(Material.SLIME_BLOCK, BlockTypes.SLIME);
        blockTypes.put(Material.BARRIER, BlockTypes.BARRIER);
        blockTypes.put(Material.IRON_TRAPDOOR, BlockTypes.IRON_TRAPDOOR);
        blockTypes.put(Material.PRISMARINE, BlockTypes.PRISMARINE);
        blockTypes.put(Material.SEA_LANTERN, BlockTypes.SEA_LANTERN);
        blockTypes.put(Material.HAY_BLOCK, BlockTypes.HAY_BLOCK);
        blockTypes.put(Material.CARPET, BlockTypes.CARPET);
        blockTypes.put(Material.HARD_CLAY, BlockTypes.HARDENED_CLAY);
        blockTypes.put(Material.COAL_BLOCK, BlockTypes.COAL_BLOCK);
        blockTypes.put(Material.PACKED_ICE, BlockTypes.PACKED_ICE);
        blockTypes.put(Material.DOUBLE_PLANT, BlockTypes.DOUBLE_PLANT);
        blockTypes.put(Material.STANDING_BANNER, BlockTypes.STANDING_BANNER);
        blockTypes.put(Material.WALL_BANNER, BlockTypes.WALL_BANNER);
        blockTypes.put(Material.DAYLIGHT_DETECTOR_INVERTED, BlockTypes.DAYLIGHT_DETECTOR_INVERTED);
        blockTypes.put(Material.RED_SANDSTONE, BlockTypes.RED_SANDSTONE);
        blockTypes.put(Material.RED_SANDSTONE_STAIRS, BlockTypes.RED_SANDSTONE_STAIRS);
        blockTypes.put(Material.DOUBLE_STONE_SLAB2, BlockTypes.DOUBLE_STONE_SLAB2);
        blockTypes.put(Material.STONE_SLAB2, BlockTypes.STONE_SLAB2);
        blockTypes.put(Material.SPRUCE_FENCE_GATE, BlockTypes.SPRUCE_FENCE_GATE);
        blockTypes.put(Material.BIRCH_FENCE_GATE, BlockTypes.BIRCH_FENCE_GATE);
        blockTypes.put(Material.JUNGLE_FENCE_GATE, BlockTypes.JUNGLE_FENCE_GATE);
        blockTypes.put(Material.DARK_OAK_FENCE_GATE, BlockTypes.DARK_OAK_FENCE_GATE);
        blockTypes.put(Material.ACACIA_FENCE_GATE, BlockTypes.ACACIA_FENCE_GATE);
        blockTypes.put(Material.SPRUCE_FENCE, BlockTypes.SPRUCE_FENCE);
        blockTypes.put(Material.BIRCH_FENCE, BlockTypes.BIRCH_FENCE);
        blockTypes.put(Material.JUNGLE_FENCE, BlockTypes.JUNGLE_FENCE);
        blockTypes.put(Material.DARK_OAK_FENCE, BlockTypes.DARK_OAK_FENCE);
        blockTypes.put(Material.ACACIA_FENCE, BlockTypes.ACACIA_FENCE);
        blockTypes.put(Material.SPRUCE_DOOR, BlockTypes.SPRUCE_DOOR);
        blockTypes.put(Material.BIRCH_DOOR, BlockTypes.BIRCH_DOOR);
        blockTypes.put(Material.JUNGLE_DOOR, BlockTypes.JUNGLE_DOOR);
        blockTypes.put(Material.ACACIA_DOOR, BlockTypes.ACACIA_DOOR);
        blockTypes.put(Material.DARK_OAK_DOOR, BlockTypes.DARK_OAK_DOOR);
        blockTypes.put(Material.END_ROD, BlockTypes.END_ROD);
        blockTypes.put(Material.CHORUS_PLANT, BlockTypes.CHORUS_PLANT);
        blockTypes.put(Material.CHORUS_FLOWER, BlockTypes.CHORUS_FLOWER);
        blockTypes.put(Material.PURPUR_BLOCK, BlockTypes.PURPUR_BLOCK);
        blockTypes.put(Material.PURPUR_PILLAR, BlockTypes.PURPUR_PILLAR);
        blockTypes.put(Material.PURPUR_STAIRS, BlockTypes.PURPUR_STAIRS);
        blockTypes.put(Material.PURPUR_DOUBLE_SLAB, BlockTypes.PURPUR_DOUBLE_SLAB);
        blockTypes.put(Material.PURPUR_SLAB, BlockTypes.PURPUR_SLAB);
        blockTypes.put(Material.END_BRICKS, BlockTypes.END_BRICKS);
        blockTypes.put(Material.BEETROOT_BLOCK, BlockTypes.BEETROOTS);
        blockTypes.put(Material.GRASS_PATH, BlockTypes.GRASS_PATH);
        blockTypes.put(Material.END_GATEWAY, BlockTypes.END_GATEWAY);
        blockTypes.put(Material.COMMAND_REPEATING, BlockTypes.REPEATING_COMMAND_BLOCK);
        blockTypes.put(Material.COMMAND_CHAIN, BlockTypes.CHAIN_COMMAND_BLOCK);
        blockTypes.put(Material.FROSTED_ICE, BlockTypes.FROSTED_ICE);
        blockTypes.put(Material.MAGMA, BlockTypes.MAGMA);
        blockTypes.put(Material.NETHER_WART_BLOCK, BlockTypes.NETHER_WART_BLOCK);
        blockTypes.put(Material.RED_NETHER_BRICK, BlockTypes.RED_NETHER_BRICK);
        blockTypes.put(Material.BONE_BLOCK, BlockTypes.BONE_BLOCK);
        blockTypes.put(Material.STRUCTURE_VOID, BlockTypes.STRUCTURE_VOID);
        blockTypes.put(Material.OBSERVER, BlockTypes.OBSERVER);
        blockTypes.put(Material.WHITE_SHULKER_BOX, BlockTypes.WHITE_SHULKER_BOX);
        blockTypes.put(Material.ORANGE_SHULKER_BOX, BlockTypes.ORANGE_SHULKER_BOX);
        blockTypes.put(Material.MAGENTA_SHULKER_BOX, BlockTypes.MAGENTA_SHULKER_BOX);
        blockTypes.put(Material.LIGHT_BLUE_SHULKER_BOX, BlockTypes.LIGHT_BLUE_SHULKER_BOX);
        blockTypes.put(Material.YELLOW_SHULKER_BOX, BlockTypes.YELLOW_SHULKER_BOX);
        blockTypes.put(Material.LIME_SHULKER_BOX, BlockTypes.LIME_SHULKER_BOX);
        blockTypes.put(Material.PINK_SHULKER_BOX, BlockTypes.PINK_SHULKER_BOX);
        blockTypes.put(Material.GRAY_SHULKER_BOX, BlockTypes.GRAY_SHULKER_BOX);
        blockTypes.put(Material.SILVER_SHULKER_BOX, BlockTypes.SILVER_SHULKER_BOX);
        blockTypes.put(Material.CYAN_SHULKER_BOX, BlockTypes.CYAN_SHULKER_BOX);
        blockTypes.put(Material.PURPLE_SHULKER_BOX, BlockTypes.PURPLE_SHULKER_BOX);
        blockTypes.put(Material.BLUE_SHULKER_BOX, BlockTypes.BLUE_SHULKER_BOX);
        blockTypes.put(Material.BROWN_SHULKER_BOX, BlockTypes.BROWN_SHULKER_BOX);
        blockTypes.put(Material.GREEN_SHULKER_BOX, BlockTypes.GREEN_SHULKER_BOX);
        blockTypes.put(Material.RED_SHULKER_BOX, BlockTypes.RED_SHULKER_BOX);
        blockTypes.put(Material.BLACK_SHULKER_BOX, BlockTypes.BLACK_SHULKER_BOX);
        blockTypes.put(Material.STRUCTURE_BLOCK, BlockTypes.STRUCTURE_BLOCK);
    }

    public static int getTypeId(BlockType type) {
        Material material = toMaterial(type);

        return material.getId();
    }

    public static Material fromTypeId(int type) {
        for (Material material : blockTypes.keySet()) {
            if (material.getId() == type) {
                return material;
            }
        }

        return Material.AIR;
    }

    public static Material toMaterial(BlockType type) {
        /*for (Map.Entry<Material, BlockType> entry : blockTypes.entrySet()) {
            if (entry.getValue().equals(type)) {
                return entry.getKey();
            }
        }

        return Material.AIR;*/
        Optional<Map.Entry<Material,BlockType>> entry = blockTypes.entrySet().stream().filter(e -> e.getValue().equals(type)).findAny();

        if (!entry.isPresent()) {
            return Material.AIR;
        }

        return entry.get().getKey();
    }

    public static BlockType toBlockType(Material material) {
        BlockType type = blockTypes.get(material);

        if (material == null) {
            return BlockTypes.AIR;
        }

        return type;
    }
}
