package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;

import java.util.Arrays;
import java.util.List;

public class IgnitableBlocks {
    private static List<Material> overwritable = Arrays.asList( Material.SAPLING, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
            Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE, Material.SNOW, Material.TORCH );

    private static List<Material> ignitable = Arrays.asList( Material.BEDROCK, Material.BOOKSHELF, Material.BRICK, Material.CLAY, Material.CLAY_BRICK,
            Material.COAL_ORE, Material.COBBLESTONE, Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.DIRT,
            Material.ENDER_STONE, Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK, Material.GRAVEL, Material.GRASS,
            Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.LOG,
            Material.MOSSY_COBBLESTONE, Material.MYCEL, Material.NETHER_BRICK, Material.NETHERRACK, Material.OBSIDIAN,
            Material.REDSTONE_ORE,
            Material.SAND,
            Material.SANDSTONE,
            Material.SMOOTH_BRICK,
            Material.STONE,
            Material.SOUL_SAND,
            Material.WOOD, // Material.SNOW_BLOCK,
            Material.WOOL, Material.LEAVES, Material.LEAVES_2, Material.MELON_BLOCK, Material.PUMPKIN,
            Material.JACK_O_LANTERN, Material.NOTE_BLOCK, Material.GLOWSTONE, Material.IRON_BLOCK, Material.DISPENSER,
            Material.SPONGE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_BLOCK, Material.WORKBENCH,
            Material.HAY_BLOCK, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.EMERALD_ORE,
            Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK, Material.QUARTZ_BLOCK, Material.QUARTZ_ORE,
            Material.STAINED_CLAY, Material.HARD_CLAY );

    public static boolean isIgnitable(Block block) {
        if (overwritable.contains(block.getType())) {
            return true;
        } else if (block.getType() != Material.AIR) {
            return false;
        }
        Block belowBlock = block.getRelative(BlockFace.DOWN);
        return ignitable.contains(belowBlock.getType());
    }
}
