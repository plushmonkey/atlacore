package com.plushnode.atlacore.platform.block;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.block.data.*;

import java.util.function.Consumer;

public enum Material {
    ACACIA_BOAT(27326, 1),
    ACACIA_BUTTON(13993),
    ACACIA_DOOR(23797),
    ACACIA_FENCE(4569),
    ACACIA_FENCE_GATE(14145),
    ACACIA_LEAVES(16606),
    ACACIA_LOG(8385),
    ACACIA_PLANKS(31312),
    ACACIA_PRESSURE_PLATE(17586),
    ACACIA_SAPLING(20806),
    ACACIA_SIGN(29808, 16),
    ACACIA_SLAB(23730),
    ACACIA_STAIRS(17453),
    ACACIA_TRAPDOOR(18343),
    ACACIA_WALL_SIGN(20316, 16),
    ACACIA_WOOD(9541),
    ACTIVATOR_RAIL(5834),
    AIR(9648, 0),
    ALLIUM(6871),
    ANDESITE(25975),
    ANDESITE_SLAB(32124),
    ANDESITE_STAIRS(17747),
    ANDESITE_WALL(14938),
    ANVIL(18718),
    APPLE(7720),
    ARMOR_STAND(12852, 16),
    ARROW(31091),
    ATTACHED_MELON_STEM(30882),
    ATTACHED_PUMPKIN_STEM(12724),
    AZURE_BLUET(17608),
    BAKED_POTATO(14624),
    BAMBOO(18728),
    BAMBOO_SAPLING(8478),
    BARREL(22396),
    BARRIER(26453),
    BAT_SPAWN_EGG(14607),
    BEACON(6608),
    BEDROCK(23130),
    BEEF(4803),
    BEETROOT(23305),
    BEETROOTS(22075),
    BEETROOT_SEEDS(21282),
    BEETROOT_SOUP(16036, 1),
    BELL(20000),
    BIRCH_BOAT(28104, 1),
    BIRCH_BUTTON(26934),
    BIRCH_DOOR(14759),
    BIRCH_FENCE(17347),
    BIRCH_FENCE_GATE(6322),
    BIRCH_LEAVES(12601),
    BIRCH_LOG(26727),
    BIRCH_PLANKS(29322),
    BIRCH_PRESSURE_PLATE(9664),
    BIRCH_SAPLING(31533),
    BIRCH_SIGN(11351, 16),
    BIRCH_SLAB(13807),
    BIRCH_STAIRS(7657),
    BIRCH_TRAPDOOR(32585),
    BIRCH_WALL_SIGN(9887, 16),
    BIRCH_WOOD(20913),
    BLACK_BANNER(9365, 16),
    BLACK_BED(20490, 1),
    BLACK_CARPET(6056),
    BLACK_CONCRETE(13338),
    BLACK_CONCRETE_POWDER(16150),
    BLACK_DYE(6202),
    BLACK_GLAZED_TERRACOTTA(29678),
    BLACK_SHULKER_BOX(24076, 1),
    BLACK_STAINED_GLASS(13941),
    BLACK_STAINED_GLASS_PANE(13201),
    BLACK_TERRACOTTA(26691),
    BLACK_WALL_BANNER(4919),
    BLACK_WOOL(16693),
    BLAST_FURNACE(31157),
    BLAZE_POWDER(18941),
    BLAZE_ROD(8289),
    BLAZE_SPAWN_EGG(4759),
    BLUE_BANNER(18481, 16),
    BLUE_BED(12714, 1),
    BLUE_CARPET(13292),
    BLUE_CONCRETE(18756),
    BLUE_CONCRETE_POWDER(17773),
    BLUE_DYE(11588),
    BLUE_GLAZED_TERRACOTTA(23823),
    BLUE_ICE(22449),
    BLUE_ORCHID(13432),
    BLUE_SHULKER_BOX(11476, 1),
    BLUE_STAINED_GLASS(7107),
    BLUE_STAINED_GLASS_PANE(28484),
    BLUE_TERRACOTTA(5236),
    BLUE_WALL_BANNER(17757),
    BLUE_WOOL(15738),
    BONE(5686),
    BONE_BLOCK(17312),
    BONE_MEAL(32458),
    BOOK(23097),
    BOOKSHELF(10069),
    BOW(8745, 1, 384),
    BOWL(32661),
    BRAIN_CORAL(31316),
    BRAIN_CORAL_BLOCK(30618),
    BRAIN_CORAL_FAN(13849),
    BRAIN_CORAL_WALL_FAN(22685),
    BREAD(32049),
    BREWING_STAND(14539),
    BRICK(6820),
    BRICKS(14165),
    BRICK_SLAB(26333),
    BRICK_STAIRS(21534),
    BRICK_WALL(18995),
    BROWN_BANNER(11481, 16),
    BROWN_BED(25624, 1),
    BROWN_CARPET(23352),
    BROWN_CONCRETE(19006),
    BROWN_CONCRETE_POWDER(21485),
    BROWN_DYE(7648),
    BROWN_GLAZED_TERRACOTTA(5655),
    BROWN_MUSHROOM(9665),
    BROWN_MUSHROOM_BLOCK(6291),
    BROWN_SHULKER_BOX(24230, 1),
    BROWN_STAINED_GLASS(20945),
    BROWN_STAINED_GLASS_PANE(17557),
    BROWN_TERRACOTTA(23664),
    BROWN_WALL_BANNER(14731),
    BROWN_WOOL(32638),
    BUBBLE_COLUMN(13758),
    BUBBLE_CORAL(12464),
    BUBBLE_CORAL_BLOCK(15437),
    BUBBLE_CORAL_FAN(10795),
    BUBBLE_CORAL_WALL_FAN(20382),
    BUCKET(15215, 16),
    CACTUS(12191),
    CAKE(27048, 1),
    CAMPFIRE(8488),
    CARROT(22824),
    CARROTS(17258),
    CARROT_ON_A_STICK(27809, 1, 25),
    CARTOGRAPHY_TABLE(28529),
    CARVED_PUMPKIN(25833),
    CAT_SPAWN_EGG(29583),
    CAULDRON(26531, Levelled.class),
    CAVE_AIR(17422),
    CAVE_SPIDER_SPAWN_EGG(23341),
    CHAINMAIL_BOOTS(17953, 1, 195),
    CHAINMAIL_CHESTPLATE(23602, 1, 240),
    CHAINMAIL_HELMET(26114, 1, 165),
    CHAINMAIL_LEGGINGS(19087, 1, 225),
    CHAIN_COMMAND_BLOCK(26798),
    CHARCOAL(5390),
    CHEST(22969),
    CHEST_MINECART(4497, 1),
    CHICKEN(17281),
    CHICKEN_SPAWN_EGG(5462),
    CHIPPED_ANVIL(10623),
    CHISELED_QUARTZ_BLOCK(30964),
    CHISELED_RED_SANDSTONE(15529),
    CHISELED_SANDSTONE(31763),
    CHISELED_STONE_BRICKS(9087),
    CHORUS_FLOWER(28542),
    CHORUS_FRUIT(7652),
    CHORUS_PLANT(28243),
    CLAY(27880),
    CLAY_BALL(24603),
    CLOCK(14980),
    COAL(29067),
    COAL_BLOCK(27968),
    COAL_ORE(30965),
    COARSE_DIRT(15411),
    COBBLESTONE(32147),
    COBBLESTONE_SLAB(6340),
    COBBLESTONE_STAIRS(24715),
    COBBLESTONE_WALL(12616),
    COBWEB(9469),
    COCOA(29709),
    COCOA_BEANS(27381),
    COD(24691),
    COD_BUCKET(28601, 1),
    COD_SPAWN_EGG(27248),
    COMMAND_BLOCK(4355),
    COMMAND_BLOCK_MINECART(7992, 1),
    COMPARATOR(18911),
    COMPASS(24139),
    COMPOSTER(31247, Levelled.class),
    CONDUIT(5148),
    COOKED_BEEF(21595),
    COOKED_CHICKEN(20780),
    COOKED_COD(9681),
    COOKED_MUTTON(31447),
    COOKED_PORKCHOP(27231),
    COOKED_RABBIT(4454),
    COOKED_SALMON(5615),
    COOKIE(27431),
    CORNFLOWER(15405),
    COW_SPAWN_EGG(14761),
    CRACKED_STONE_BRICKS(27869),
    CRAFTING_TABLE(20706),
    CREEPER_BANNER_PATTERN(15774, 1),
    CREEPER_HEAD(29146),
    CREEPER_SPAWN_EGG(9653),
    CREEPER_WALL_HEAD(30123),
    CROSSBOW(4340, 1, 326),
    CUT_RED_SANDSTONE(26842),
    CUT_RED_SANDSTONE_SLAB(7220),
    CUT_SANDSTONE(6118),
    CUT_SANDSTONE_SLAB(30944),
    CYAN_BANNER(9839, 16),
    CYAN_BED(16746, 1),
    CYAN_CARPET(31495),
    CYAN_CONCRETE(26522),
    CYAN_CONCRETE_POWDER(15734),
    CYAN_DYE(8043),
    CYAN_GLAZED_TERRACOTTA(9550),
    CYAN_SHULKER_BOX(28123, 1),
    CYAN_STAINED_GLASS(30604),
    CYAN_STAINED_GLASS_PANE(11784),
    CYAN_TERRACOTTA(25940),
    CYAN_WALL_BANNER(10889),
    CYAN_WOOL(12221),
    DAMAGED_ANVIL(10274),
    DANDELION(30558),
    DARK_OAK_BOAT(28618, 1),
    DARK_OAK_BUTTON(6214),
    DARK_OAK_DOOR(10669),
    DARK_OAK_FENCE(21767),
    DARK_OAK_FENCE_GATE(10679),
    DARK_OAK_LEAVES(22254),
    DARK_OAK_LOG(14831),
    DARK_OAK_PLANKS(20869),
    DARK_OAK_PRESSURE_PLATE(31375),
    DARK_OAK_SAPLING(14933),
    DARK_OAK_SIGN(15127, 16),
    DARK_OAK_SLAB(28852),
    DARK_OAK_STAIRS(22921),
    DARK_OAK_TRAPDOOR(10355),
    DARK_OAK_WALL_SIGN(9508, 16),
    DARK_OAK_WOOD(16995),
    DARK_PRISMARINE(19940),
    DARK_PRISMARINE_SLAB(7577),
    DARK_PRISMARINE_STAIRS(26511),
    DAYLIGHT_DETECTOR(8864),
    DEAD_BRAIN_CORAL(9116),
    DEAD_BRAIN_CORAL_BLOCK(12979),
    DEAD_BRAIN_CORAL_FAN(26150),
    DEAD_BRAIN_CORAL_WALL_FAN(23718),
    DEAD_BUBBLE_CORAL(30583),
    DEAD_BUBBLE_CORAL_BLOCK(28220),
    DEAD_BUBBLE_CORAL_FAN(17322),
    DEAD_BUBBLE_CORAL_WALL_FAN(18453),
    DEAD_BUSH(22888),
    DEAD_FIRE_CORAL(8365),
    DEAD_FIRE_CORAL_BLOCK(5307),
    DEAD_FIRE_CORAL_FAN(27073),
    DEAD_FIRE_CORAL_WALL_FAN(23375),
    DEAD_HORN_CORAL(5755),
    DEAD_HORN_CORAL_BLOCK(15103),
    DEAD_HORN_CORAL_FAN(11387),
    DEAD_HORN_CORAL_WALL_FAN(27550),
    DEAD_TUBE_CORAL(18028),
    DEAD_TUBE_CORAL_BLOCK(28350),
    DEAD_TUBE_CORAL_FAN(17628),
    DEAD_TUBE_CORAL_WALL_FAN(5128),
    DEBUG_STICK(24562, 1),
    DETECTOR_RAIL(13475),
    DIAMOND(20865),
    DIAMOND_AXE(27277, 1, 1561),
    DIAMOND_BLOCK(5944),
    DIAMOND_BOOTS(16522, 1, 429),
    DIAMOND_CHESTPLATE(32099, 1, 528),
    DIAMOND_HELMET(10755, 1, 363),
    DIAMOND_HOE(24050, 1, 1561),
    DIAMOND_HORSE_ARMOR(10321, 1),
    DIAMOND_LEGGINGS(11202, 1, 495),
    DIAMOND_ORE(9292),
    DIAMOND_PICKAXE(24291, 1, 1561),
    DIAMOND_SHOVEL(25415, 1, 1561),
    DIAMOND_SWORD(27707, 1, 1561),
    DIORITE(24688),
    DIORITE_SLAB(10715),
    DIORITE_STAIRS(13134),
    DIORITE_WALL(17412),
    DIRT(10580),
    DISPENSER(20871),
    DOLPHIN_SPAWN_EGG(20787),
    DONKEY_SPAWN_EGG(14513),
    DRAGON_BREATH(20154),
    DRAGON_EGG(29946),
    DRAGON_HEAD(20084),
    DRAGON_WALL_HEAD(19818),
    DRIED_KELP(21042),
    DRIED_KELP_BLOCK(12966),
    DROPPER(31273),
    DROWNED_SPAWN_EGG(19368),
    EGG(21603, 16),
    ELDER_GUARDIAN_SPAWN_EGG(11418),
    ELYTRA(23829, 1, 432),
    EMERALD(5654),
    EMERALD_BLOCK(9914),
    EMERALD_ORE(16630),
    ENCHANTED_BOOK(11741, 1),
    ENCHANTED_GOLDEN_APPLE(8280),
    ENCHANTING_TABLE(16255),
    ENDERMAN_SPAWN_EGG(29488),
    ENDERMITE_SPAWN_EGG(16617),
    ENDER_CHEST(32349),
    ENDER_EYE(24860),
    ENDER_PEARL(5259, 16),
    END_CRYSTAL(19090),
    END_GATEWAY(26605),
    END_PORTAL(16782),
    END_PORTAL_FRAME(15480),
    END_ROD(24832),
    END_STONE(29686),
    END_STONE_BRICKS(20314),
    END_STONE_BRICK_SLAB(23239),
    END_STONE_BRICK_STAIRS(28831),
    END_STONE_BRICK_WALL(27225),
    EVOKER_SPAWN_EGG(21271),
    EXPERIENCE_BOTTLE(12858),
    FARMLAND(31166),
    FEATHER(30548),
    FERMENTED_SPIDER_EYE(19386),
    FERN(15794),
    FILLED_MAP(23504),
    FIRE(16396),
    FIREWORK_ROCKET(23841),
    FIREWORK_STAR(12190),
    FIRE_CHARGE(4842),
    FIRE_CORAL(29151),
    FIRE_CORAL_BLOCK(12119),
    FIRE_CORAL_FAN(11112),
    FIRE_CORAL_WALL_FAN(20100),
    FISHING_ROD(4167, 1, 64),
    FLETCHING_TABLE(30838),
    FLINT(23596),
    FLINT_AND_STEEL(28620, 1, 64),
    FLOWER_BANNER_PATTERN(5762, 1),
    FLOWER_POT(30567),
    FOX_SPAWN_EGG(22376),
    FROSTED_ICE(21814),
    FURNACE(8133),
    FURNACE_MINECART(14196, 1),
    GHAST_SPAWN_EGG(9970),
    GHAST_TEAR(18222),
    GLASS(6195),
    GLASS_BOTTLE(6116),
    GLASS_PANE(5709),
    GLISTERING_MELON_SLICE(20158),
    GLOBE_BANNER_PATTERN(27753, 1),
    GLOWSTONE(32713),
    GLOWSTONE_DUST(6665),
    GOLDEN_APPLE(27732),
    GOLDEN_AXE(4878, 1, 32),
    GOLDEN_BOOTS(7859, 1, 91),
    GOLDEN_CARROT(5300),
    GOLDEN_CHESTPLATE(4507, 1, 112),
    GOLDEN_HELMET(7945, 1, 77),
    GOLDEN_HOE(19337, 1, 32),
    GOLDEN_HORSE_ARMOR(7996, 1),
    GOLDEN_LEGGINGS(21002, 1, 105),
    GOLDEN_PICKAXE(10901, 1, 32),
    GOLDEN_SHOVEL(15597, 1, 32),
    GOLDEN_SWORD(10505, 1, 32),
    GOLD_BLOCK(27392),
    GOLD_INGOT(28927),
    GOLD_NUGGET(28814),
    GOLD_ORE(32625),
    GRANITE(21091),
    GRANITE_SLAB(25898),
    GRANITE_STAIRS(21840),
    GRANITE_WALL(23279),
    GRASS(6155),
    GRASS_BLOCK(28346),
    GRASS_PATH(8604),
    GRAVEL(7804),
    GRAY_BANNER(12053, 16),
    GRAY_BED(15745, 1),
    GRAY_CARPET(26991),
    GRAY_CONCRETE(13959),
    GRAY_CONCRETE_POWDER(13031),
    GRAY_DYE(9184),
    GRAY_GLAZED_TERRACOTTA(6256),
    GRAY_SHULKER_BOX(12754, 1),
    GRAY_STAINED_GLASS(29979),
    GRAY_STAINED_GLASS_PANE(25272),
    GRAY_TERRACOTTA(18004),
    GRAY_WALL_BANNER(24275),
    GRAY_WOOL(27209),
    GREEN_BANNER(10698, 16),
    GREEN_BED(13797, 1),
    GREEN_CARPET(7780),
    GREEN_CONCRETE(17949),
    GREEN_CONCRETE_POWDER(6904),
    GREEN_DYE(23215),
    GREEN_GLAZED_TERRACOTTA(6958),
    GREEN_SHULKER_BOX(9377, 1),
    GREEN_STAINED_GLASS(22503),
    GREEN_STAINED_GLASS_PANE(4767),
    GREEN_TERRACOTTA(4105),
    GREEN_WALL_BANNER(15046),
    GREEN_WOOL(25085),
    GRINDSTONE(26260),
    GUARDIAN_SPAWN_EGG(20113),
    GUNPOWDER(29974),
    HAY_BLOCK(17461),
    HEART_OF_THE_SEA(11807),
    HEAVY_WEIGHTED_PRESSURE_PLATE(16970),
    HOPPER(31974),
    HOPPER_MINECART(19024, 1),
    HORN_CORAL(19511),
    HORN_CORAL_BLOCK(19958),
    HORN_CORAL_FAN(13610),
    HORN_CORAL_WALL_FAN(28883),
    HORSE_SPAWN_EGG(25981),
    HUSK_SPAWN_EGG(20178),
    ICE(30428),
    INFESTED_CHISELED_STONE_BRICKS(4728),
    INFESTED_COBBLESTONE(28798),
    INFESTED_CRACKED_STONE_BRICKS(7476),
    INFESTED_MOSSY_STONE_BRICKS(9850),
    INFESTED_STONE(18440),
    INFESTED_STONE_BRICKS(19749),
    INK_SAC(7184),
    IRON_AXE(15894, 1, 250),
    IRON_BARS(9378),
    IRON_BLOCK(24754),
    IRON_BOOTS(8531, 1, 195),
    IRON_CHESTPLATE(28112, 1, 240),
    IRON_DOOR(4788),
    IRON_HELMET(12025, 1, 165),
    IRON_HOE(11339, 1, 250),
    IRON_HORSE_ARMOR(30108, 1),
    IRON_INGOT(24895),
    IRON_LEGGINGS(18951, 1, 225),
    IRON_NUGGET(13715),
    IRON_ORE(19834),
    IRON_PICKAXE(8842, 1, 250),
    IRON_SHOVEL(30045, 1, 250),
    IRON_SWORD(10904, 1, 250),
    IRON_TRAPDOOR(17095),
    ITEM_FRAME(27318),
    JACK_O_LANTERN(31612),
    JIGSAW(17398),
    JUKEBOX(19264),
    JUNGLE_BOAT(4495, 1),
    JUNGLE_BUTTON(25317),
    JUNGLE_DOOR(28163),
    JUNGLE_FENCE(14358),
    JUNGLE_FENCE_GATE(21360),
    JUNGLE_LEAVES(5133),
    JUNGLE_LOG(20721),
    JUNGLE_PLANKS(26445),
    JUNGLE_PRESSURE_PLATE(11376),
    JUNGLE_SAPLING(17951),
    JUNGLE_SIGN(24717, 16),
    JUNGLE_SLAB(19117),
    JUNGLE_STAIRS(20636),
    JUNGLE_TRAPDOOR(8626),
    JUNGLE_WALL_SIGN(29629, 16),
    JUNGLE_WOOD(10341),
    KELP(21916),
    KELP_PLANT(29697),
    KNOWLEDGE_BOOK(12646, 1),
    LADDER(23599),
    LANTERN(5992),
    LAPIS_BLOCK(14485),
    LAPIS_LAZULI(11075),
    LAPIS_ORE(22934),
    LARGE_FERN(30177),
    LAVA(8415, Levelled.class),
    LAVA_BUCKET(9228, 1),
    LEAD(29539),
    LEATHER(16414),
    LEATHER_BOOTS(15282, 1, 65),
    LEATHER_CHESTPLATE(29275, 1, 80),
    LEATHER_HELMET(11624, 1, 55),
    LEATHER_HORSE_ARMOR(30667, 1),
    LEATHER_LEGGINGS(28210, 1, 75),
    LECTERN(23490),
    LEVER(15319),
    LIGHT_BLUE_BANNER(18060, 16),
    LIGHT_BLUE_BED(20957, 1),
    LIGHT_BLUE_CARPET(21194),
    LIGHT_BLUE_CONCRETE(29481),
    LIGHT_BLUE_CONCRETE_POWDER(31206),
    LIGHT_BLUE_DYE(28738),
    LIGHT_BLUE_GLAZED_TERRACOTTA(4336),
    LIGHT_BLUE_SHULKER_BOX(18226, 1),
    LIGHT_BLUE_STAINED_GLASS(17162),
    LIGHT_BLUE_STAINED_GLASS_PANE(18721),
    LIGHT_BLUE_TERRACOTTA(31779),
    LIGHT_BLUE_WALL_BANNER(12011),
    LIGHT_BLUE_WOOL(21073),
    LIGHT_GRAY_BANNER(11417, 16),
    LIGHT_GRAY_BED(5090, 1),
    LIGHT_GRAY_CARPET(11317),
    LIGHT_GRAY_CONCRETE(14453),
    LIGHT_GRAY_CONCRETE_POWDER(21589),
    LIGHT_GRAY_DYE(27643),
    LIGHT_GRAY_GLAZED_TERRACOTTA(10707),
    LIGHT_GRAY_SHULKER_BOX(21345, 1),
    LIGHT_GRAY_STAINED_GLASS(5843),
    LIGHT_GRAY_STAINED_GLASS_PANE(19008),
    LIGHT_GRAY_TERRACOTTA(26388),
    LIGHT_GRAY_WALL_BANNER(31088),
    LIGHT_GRAY_WOOL(22936),
    LIGHT_WEIGHTED_PRESSURE_PLATE(14875),
    LILAC(22837),
    LILY_OF_THE_VALLEY(7185),
    LILY_PAD(19271),
    LIME_BANNER(18887, 16),
    LIME_BED(27860, 1),
    LIME_CARPET(15443),
    LIME_CONCRETE(5863),
    LIME_CONCRETE_POWDER(28859),
    LIME_DYE(6147),
    LIME_GLAZED_TERRACOTTA(13861),
    LIME_SHULKER_BOX(28360, 1),
    LIME_STAINED_GLASS(24266),
    LIME_STAINED_GLASS_PANE(10610),
    LIME_TERRACOTTA(24013),
    LIME_WALL_BANNER(21422),
    LIME_WOOL(10443),
    LINGERING_POTION(25857, 1),
    LLAMA_SPAWN_EGG(23640),
    LOOM(14276),
    MAGENTA_BANNER(15591, 16),
    MAGENTA_BED(20061, 1),
    MAGENTA_CARPET(6180),
    MAGENTA_CONCRETE(20591),
    MAGENTA_CONCRETE_POWDER(8272),
    MAGENTA_DYE(11788),
    MAGENTA_GLAZED_TERRACOTTA(8067),
    MAGENTA_SHULKER_BOX(21566, 1),
    MAGENTA_STAINED_GLASS(26814),
    MAGENTA_STAINED_GLASS_PANE(14082),
    MAGENTA_TERRACOTTA(25900),
    MAGENTA_WALL_BANNER(23291),
    MAGENTA_WOOL(11853),
    MAGMA_BLOCK(25927),
    MAGMA_CREAM(25097),
    MAGMA_CUBE_SPAWN_EGG(26638),
    MAP(21655),
    MELON(25172),
    MELON_SEEDS(18340),
    MELON_SLICE(5347),
    MELON_STEM(8247),
    MILK_BUCKET(9680, 1),
    MINECART(14352, 1),
    MOJANG_BANNER_PATTERN(11903, 1),
    MOOSHROOM_SPAWN_EGG(22125),
    MOSSY_COBBLESTONE(21900),
    MOSSY_COBBLESTONE_SLAB(12139),
    MOSSY_COBBLESTONE_STAIRS(29210),
    MOSSY_COBBLESTONE_WALL(11536),
    MOSSY_STONE_BRICKS(16415),
    MOSSY_STONE_BRICK_SLAB(14002),
    MOSSY_STONE_BRICK_STAIRS(27578),
    MOSSY_STONE_BRICK_WALL(18259),
    MOVING_PISTON(13831),
    MULE_SPAWN_EGG(11229),
    MUSHROOM_STEM(16543),
    MUSHROOM_STEW(16336, 1),
    MUSIC_DISC_11(27426, 1),
    MUSIC_DISC_13(16359, 1),
    MUSIC_DISC_BLOCKS(26667, 1),
    MUSIC_DISC_CAT(16246, 1),
    MUSIC_DISC_CHIRP(19436, 1),
    MUSIC_DISC_FAR(13823, 1),
    MUSIC_DISC_MALL(11517, 1),
    MUSIC_DISC_MELLOHI(26117, 1),
    MUSIC_DISC_STAL(14989, 1),
    MUSIC_DISC_STRAD(16785, 1),
    MUSIC_DISC_WAIT(26499, 1),
    MUSIC_DISC_WARD(24026, 1),
    MUTTON(4792),
    MYCELIUM(9913),
    NAME_TAG(30731),
    NAUTILUS_SHELL(19989),
    NETHERRACK(23425),
    NETHER_BRICK(19996),
    NETHER_BRICKS(27802),
    NETHER_BRICK_FENCE(5286),
    NETHER_BRICK_SLAB(26586),
    NETHER_BRICK_STAIRS(12085),
    NETHER_BRICK_WALL(10398),
    NETHER_PORTAL(19469),
    NETHER_QUARTZ_ORE(4807),
    NETHER_STAR(12469),
    NETHER_WART(29227),
    NETHER_WART_BLOCK(15486),
    NOTE_BLOCK(20979),
    OAK_BOAT(17570, 1),
    OAK_BUTTON(13510),
    OAK_DOOR(20341),
    OAK_FENCE(6442),
    OAK_FENCE_GATE(16689),
    OAK_LEAVES(4385),
    OAK_LOG(26723),
    OAK_PLANKS(14905),
    OAK_PRESSURE_PLATE(20108),
    OAK_SAPLING(9636),
    OAK_SIGN(8192, 16),
    OAK_SLAB(12002),
    OAK_STAIRS(5449),
    OAK_TRAPDOOR(16927),
    OAK_WALL_SIGN(12984, 16),
    OAK_WOOD(7378),
    OBSERVER(10726),
    OBSIDIAN(32723),
    OCELOT_SPAWN_EGG(30080),
    ORANGE_BANNER(4839, 16),
    ORANGE_BED(11194, 1),
    ORANGE_CARPET(24752),
    ORANGE_CONCRETE(19914),
    ORANGE_CONCRETE_POWDER(30159),
    ORANGE_DYE(13866),
    ORANGE_GLAZED_TERRACOTTA(27451),
    ORANGE_SHULKER_BOX(21673, 1),
    ORANGE_STAINED_GLASS(25142),
    ORANGE_STAINED_GLASS_PANE(21089),
    ORANGE_TERRACOTTA(18684),
    ORANGE_TULIP(26038),
    ORANGE_WALL_BANNER(9936),
    ORANGE_WOOL(23957),
    OXEYE_DAISY(11709),
    PACKED_ICE(28993),
    PAINTING(23945),
    PANDA_SPAWN_EGG(23759),
    PAPER(9923),
    PARROT_SPAWN_EGG(23614),
    PEONY(21155),
    PETRIFIED_OAK_SLAB(18658),
    PHANTOM_MEMBRANE(18398),
    PHANTOM_SPAWN_EGG(24648),
    PIG_SPAWN_EGG(22584),
    PILLAGER_SPAWN_EGG(28659),
    PINK_BANNER(19439, 16),
    PINK_BED(13795, 1),
    PINK_CARPET(30186),
    PINK_CONCRETE(5227),
    PINK_CONCRETE_POWDER(6421),
    PINK_DYE(31151),
    PINK_GLAZED_TERRACOTTA(10260),
    PINK_SHULKER_BOX(24968, 1),
    PINK_STAINED_GLASS(16164),
    PINK_STAINED_GLASS_PANE(24637),
    PINK_TERRACOTTA(23727),
    PINK_TULIP(27319),
    PINK_WALL_BANNER(9421),
    PINK_WOOL(7611),
    PISTON(21130),
    PISTON_HEAD(30226),
    PLAYER_HEAD(21174),
    PLAYER_WALL_HEAD(13164),
    PODZOL(24068),
    POISONOUS_POTATO(32640),
    POLAR_BEAR_SPAWN_EGG(17015),
    POLISHED_ANDESITE(8335),
    POLISHED_ANDESITE_SLAB(24573),
    POLISHED_ANDESITE_STAIRS(7573),
    POLISHED_DIORITE(31615),
    POLISHED_DIORITE_SLAB(18303),
    POLISHED_DIORITE_STAIRS(4625),
    POLISHED_GRANITE(5477),
    POLISHED_GRANITE_SLAB(4521),
    POLISHED_GRANITE_STAIRS(29588),
    POPPED_CHORUS_FRUIT(27844),
    POPPY(12851),
    PORKCHOP(30896),
    POTATO(21088),
    POTATOES(10879),
    POTION(24020, 1),
    POTTED_ACACIA_SAPLING(14096),
    POTTED_ALLIUM(13184),
    POTTED_AZURE_BLUET(8754),
    POTTED_BAMBOO(22542),
    POTTED_BIRCH_SAPLING(32484),
    POTTED_BLUE_ORCHID(6599),
    POTTED_BROWN_MUSHROOM(14481),
    POTTED_CACTUS(8777),
    POTTED_CORNFLOWER(28917),
    POTTED_DANDELION(9727),
    POTTED_DARK_OAK_SAPLING(6486),
    POTTED_DEAD_BUSH(13020),
    POTTED_FERN(23315),
    POTTED_JUNGLE_SAPLING(7525),
    POTTED_LILY_OF_THE_VALLEY(9364),
    POTTED_OAK_SAPLING(11905),
    POTTED_ORANGE_TULIP(28807),
    POTTED_OXEYE_DAISY(19707),
    POTTED_PINK_TULIP(10089),
    POTTED_POPPY(7457),
    POTTED_RED_MUSHROOM(22881),
    POTTED_RED_TULIP(28594),
    POTTED_SPRUCE_SAPLING(29498),
    POTTED_WHITE_TULIP(24330),
    POTTED_WITHER_ROSE(26876),
    POWERED_RAIL(11064),
    PRISMARINE(7539),
    PRISMARINE_BRICKS(29118),
    PRISMARINE_BRICK_SLAB(26672),
    PRISMARINE_BRICK_STAIRS(15445),
    PRISMARINE_CRYSTALS(31546),
    PRISMARINE_SHARD(10993),
    PRISMARINE_SLAB(31323),
    PRISMARINE_STAIRS(19217),
    PRISMARINE_WALL(18184),
    PUFFERFISH(8115),
    PUFFERFISH_BUCKET(8861, 1),
    PUFFERFISH_SPAWN_EGG(24570),
    PUMPKIN(19170),
    PUMPKIN_PIE(28725),
    PUMPKIN_SEEDS(28985),
    PUMPKIN_STEM(19021),
    PURPLE_BANNER(29027, 16),
    PURPLE_BED(29755, 1),
    PURPLE_CARPET(5574),
    PURPLE_CONCRETE(20623),
    PURPLE_CONCRETE_POWDER(26808),
    PURPLE_DYE(6347),
    PURPLE_GLAZED_TERRACOTTA(4818),
    PURPLE_SHULKER_BOX(10373, 1),
    PURPLE_STAINED_GLASS(21845),
    PURPLE_STAINED_GLASS_PANE(10948),
    PURPLE_TERRACOTTA(10387),
    PURPLE_WALL_BANNER(14298),
    PURPLE_WOOL(11922),
    PURPUR_BLOCK(7538),
    PURPUR_PILLAR(26718),
    PURPUR_SLAB(11487),
    PURPUR_STAIRS(8921),
    QUARTZ(23608),
    QUARTZ_BLOCK(11987),
    QUARTZ_PILLAR(16452),
    QUARTZ_SLAB(4423),
    QUARTZ_STAIRS(24079),
    RABBIT(23068),
    RABBIT_FOOT(13864),
    RABBIT_HIDE(12467),
    RABBIT_SPAWN_EGG(26496),
    RABBIT_STEW(10611, 1),
    RAIL(13285),
    RAVAGER_SPAWN_EGG(8726),
    REDSTONE(11233),
    REDSTONE_BLOCK(19496),
    REDSTONE_LAMP(8217),
    REDSTONE_ORE(10887),
    REDSTONE_TORCH(22547),
    REDSTONE_WALL_TORCH(7595),
    REDSTONE_WIRE(25984),
    RED_BANNER(26961, 16),
    RED_BED(30910, 1),
    RED_CARPET(5424),
    RED_CONCRETE(8032),
    RED_CONCRETE_POWDER(13286),
    RED_DYE(5728),
    RED_GLAZED_TERRACOTTA(24989),
    RED_MUSHROOM(19728),
    RED_MUSHROOM_BLOCK(20766),
    RED_NETHER_BRICKS(18056),
    RED_NETHER_BRICK_SLAB(12462),
    RED_NETHER_BRICK_STAIRS(26374),
    RED_NETHER_BRICK_WALL(4580),
    RED_SAND(16279),
    RED_SANDSTONE(9092),
    RED_SANDSTONE_SLAB(17550),
    RED_SANDSTONE_STAIRS(25466),
    RED_SANDSTONE_WALL(4753),
    RED_SHULKER_BOX(32448, 1),
    RED_STAINED_GLASS(9717),
    RED_STAINED_GLASS_PANE(8630),
    RED_TERRACOTTA(5086),
    RED_TULIP(16781),
    RED_WALL_BANNER(4378),
    RED_WOOL(11621),
    REPEATER(28823),
    REPEATING_COMMAND_BLOCK(12405),
    ROSE_BUSH(6080),
    ROTTEN_FLESH(21591),
    SADDLE(30206, 1),
    SALMON(18516),
    SALMON_BUCKET(31427, 1),
    SALMON_SPAWN_EGG(18739),
    SAND(11542),
    SANDSTONE(13141),
    SANDSTONE_SLAB(29830),
    SANDSTONE_STAIRS(18474),
    SANDSTONE_WALL(18470),
    SCAFFOLDING(15757),
    SCUTE(11914),
    SEAGRASS(23942),
    SEA_LANTERN(16984),
    SEA_PICKLE(19562),
    SHEARS(27971, 1, 238),
    SHEEP_SPAWN_EGG(24488),
    SHIELD(29943, 1, 336),
    SHULKER_BOX(7776, 1),
    SHULKER_SHELL(27848),
    SHULKER_SPAWN_EGG(31848),
    SILVERFISH_SPAWN_EGG(14537),
    SKELETON_HORSE_SPAWN_EGG(21356),
    SKELETON_SKULL(13270),
    SKELETON_SPAWN_EGG(15261),
    SKELETON_WALL_SKULL(31650),
    SKULL_BANNER_PATTERN(7680, 1),
    SLIME_BALL(5242),
    SLIME_BLOCK(31892),
    SLIME_SPAWN_EGG(6550),
    SMITHING_TABLE(9082),
    SMOKER(24781),
    SMOOTH_QUARTZ(14415),
    SMOOTH_QUARTZ_SLAB(26543),
    SMOOTH_QUARTZ_STAIRS(19560),
    SMOOTH_RED_SANDSTONE(25180),
    SMOOTH_RED_SANDSTONE_SLAB(16304),
    SMOOTH_RED_SANDSTONE_STAIRS(17561),
    SMOOTH_SANDSTONE(30039),
    SMOOTH_SANDSTONE_SLAB(9030),
    SMOOTH_SANDSTONE_STAIRS(21183),
    SMOOTH_STONE(21910),
    SMOOTH_STONE_SLAB(24129),
    SNOW(14146),
    SNOWBALL(19487, 16),
    SNOW_BLOCK(19913),
    SOUL_SAND(16841),
    SPAWNER(7018),
    SPECTRAL_ARROW(4568),
    SPIDER_EYE(9318),
    SPIDER_SPAWN_EGG(14984),
    SPLASH_POTION(30248, 1),
    SPONGE(15860),
    SPRUCE_BOAT(9606, 1),
    SPRUCE_BUTTON(23281),
    SPRUCE_DOOR(10642),
    SPRUCE_FENCE(25416),
    SPRUCE_FENCE_GATE(26423),
    SPRUCE_LEAVES(20039),
    SPRUCE_LOG(9726),
    SPRUCE_PLANKS(14593),
    SPRUCE_PRESSURE_PLATE(15932),
    SPRUCE_SAPLING(19874),
    SPRUCE_SIGN(21502, 16),
    SPRUCE_SLAB(4348),
    SPRUCE_STAIRS(11192),
    SPRUCE_TRAPDOOR(10289),
    SPRUCE_WALL_SIGN(7352, 16),
    SPRUCE_WOOD(32328),
    SQUID_SPAWN_EGG(10682),
    STICK(9773),
    STICKY_PISTON(18127),
    STONE(22948),
    STONECUTTER(25170),
    STONE_AXE(6338, 1, 131),
    STONE_BRICKS(6962),
    STONE_BRICK_SLAB(19676),
    STONE_BRICK_STAIRS(27032),
    STONE_BRICK_WALL(29073),
    STONE_BUTTON(12279),
    STONE_HOE(22855, 1, 131),
    STONE_PICKAXE(14611, 1, 131),
    STONE_PRESSURE_PLATE(22591),
    STONE_SHOVEL(9520, 1, 131),
    STONE_SLAB(19838),
    STONE_STAIRS(23784),
    STONE_SWORD(25084, 1, 131),
    STRAY_SPAWN_EGG(30153),
    STRING(12806),
    STRIPPED_ACACIA_LOG(18167),
    STRIPPED_ACACIA_WOOD(27193),
    STRIPPED_BIRCH_LOG(8838),
    STRIPPED_BIRCH_WOOD(22350),
    STRIPPED_DARK_OAK_LOG(6492),
    STRIPPED_DARK_OAK_WOOD(16000),
    STRIPPED_JUNGLE_LOG(15476),
    STRIPPED_JUNGLE_WOOD(30315),
    STRIPPED_OAK_LOG(20523),
    STRIPPED_OAK_WOOD(31455),
    STRIPPED_SPRUCE_LOG(6140),
    STRIPPED_SPRUCE_WOOD(6467),
    STRUCTURE_BLOCK(26831),
    STRUCTURE_VOID(30806),
    SUGAR(30638),
    SUGAR_CANE(7726),
    SUNFLOWER(7408),
    SUSPICIOUS_STEW(8173, 1),
    SWEET_BERRIES(19747),
    SWEET_BERRY_BUSH(11958),
    TALL_GRASS(21559),
    TALL_SEAGRASS(27189),
    TERRACOTTA(16544),
    TIPPED_ARROW(25164),
    TNT(7896),
    TNT_MINECART(4277, 1),
    TORCH(6063),
    TOTEM_OF_UNDYING(10139, 1),
    TRADER_LLAMA_SPAWN_EGG(8439),
    TRAPPED_CHEST(18970),
    TRIDENT(7534, 1, 250),
    TRIPWIRE(8810),
    TRIPWIRE_HOOK(8130),
    TROPICAL_FISH(24879),
    TROPICAL_FISH_BUCKET(29995, 1),
    TROPICAL_FISH_SPAWN_EGG(19713),
    TUBE_CORAL(23048),
    TUBE_CORAL_BLOCK(23723),
    TUBE_CORAL_FAN(19929),
    TUBE_CORAL_WALL_FAN(25282),
    TURTLE_EGG(32101),
    TURTLE_HELMET(30120, 1, 275),
    TURTLE_SPAWN_EGG(17324),
    VEX_SPAWN_EGG(27751),
    VILLAGER_SPAWN_EGG(30348),
    VINDICATOR_SPAWN_EGG(25324),
    VINE(14564),
    VOID_AIR(13668),
    WALL_TORCH(25890),
    WANDERING_TRADER_SPAWN_EGG(17904),
    WATER(24998, Levelled.class),
    WATER_BUCKET(8802, 1),
    WET_SPONGE(9043),
    WHEAT(27709),
    WHEAT_SEEDS(28742),
    WHITE_BANNER(17562, 16),
    WHITE_BED(8185, 1),
    WHITE_CARPET(15117),
    WHITE_CONCRETE(6281),
    WHITE_CONCRETE_POWDER(10363),
    WHITE_DYE(10758),
    WHITE_GLAZED_TERRACOTTA(11326),
    WHITE_SHULKER_BOX(31750, 1),
    WHITE_STAINED_GLASS(31190),
    WHITE_STAINED_GLASS_PANE(10557),
    WHITE_TERRACOTTA(20975),
    WHITE_TULIP(9742),
    WHITE_WALL_BANNER(15967),
    WHITE_WOOL(8624),
    WITCH_SPAWN_EGG(11837),
    WITHER_ROSE(8619),
    WITHER_SKELETON_SKULL(31487),
    WITHER_SKELETON_SPAWN_EGG(10073),
    WITHER_SKELETON_WALL_SKULL(9326),
    WOLF_SPAWN_EGG(21692),
    WOODEN_AXE(6292, 1, 59),
    WOODEN_HOE(16043, 1, 59),
    WOODEN_PICKAXE(12792, 1, 59),
    WOODEN_SHOVEL(28432, 1, 59),
    WOODEN_SWORD(7175, 1, 59),
    WRITABLE_BOOK(13393, 1),
    WRITTEN_BOOK(24164, 16),
    YELLOW_BANNER(30382, 16),
    YELLOW_BED(30410, 1),
    YELLOW_CARPET(18149),
    YELLOW_CONCRETE(15722),
    YELLOW_CONCRETE_POWDER(10655),
    YELLOW_DYE(5952),
    YELLOW_GLAZED_TERRACOTTA(10914),
    YELLOW_SHULKER_BOX(28700, 1),
    YELLOW_STAINED_GLASS(12182),
    YELLOW_STAINED_GLASS_PANE(20298),
    YELLOW_TERRACOTTA(32129),
    YELLOW_WALL_BANNER(32004),
    YELLOW_WOOL(29507),
    ZOMBIE_HEAD(9304),
    ZOMBIE_HORSE_SPAWN_EGG(4275),
    ZOMBIE_PIGMAN_SPAWN_EGG(11531),
    ZOMBIE_SPAWN_EGG(5814),
    ZOMBIE_VILLAGER_SPAWN_EGG(10311),
    ZOMBIE_WALL_HEAD(16296),
    ;

    private final int id;
    private final int stack;
    private final int durability;
    private final Class<?> data;

    Material(final int id) {
        this(id, 64);
    }

    Material(final int id, final int stack) {
        this(id, stack, 0);
    }

    Material(final int id, final int stack, final int durability) {
        this(id, stack, durability, null);
    }

    Material(final int id, final Class<?> data) {
        this(id, 64, data);
    }

    Material(final int id, final int stack, final Class<?> data) {
        this(id, stack, 0, data);
    }

    Material(final int id, final int stack, final int durability, final Class<?> data) {
        this.id = id;
        this.stack = stack;
        this.durability = durability;
        this.data = data;
    }

    public Class<?> getDataClass() {
        return this.data;
    }

    public String getId() {
        return "minecraft:" + this.toString().toLowerCase();
    }

    public BlockData createBlockData() {
        return Game.plugin.createBlockData(this);
    }

    public BlockData createBlockData(Consumer<BlockData> consumer) {
        BlockData data = Game.plugin.createBlockData(this);

        consumer.accept(data);

        return data;
    }

    public static Material getFromId(String id) {
        if (id.contains(":")) {
            id = id.split(":", 2)[1];
        }

        try {
            return Material.valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
