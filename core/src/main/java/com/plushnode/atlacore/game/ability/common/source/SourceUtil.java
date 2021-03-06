package com.plushnode.atlacore.game.ability.common.source;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.water.PhaseChange;
import com.plushnode.atlacore.game.ability.water.util.BottleReturn;
import com.plushnode.atlacore.platform.ItemStack;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class SourceUtil {
    private SourceUtil() {

    }

    public static Optional<Block> getSource(User user, double range, SourceTypes types) {
        List<Material> materials = new ArrayList<>();

        if (types.contains(SourceType.Water)) {
            materials.add(Material.WATER);
        }

        if (types.contains(SourceType.Plant)) {
            materials.addAll(Arrays.asList(MaterialUtil.getPlantMaterials()));
        }

        if (types.contains(SourceType.Ice)) {
            materials.add(Material.ICE);
            materials.add(Material.PACKED_ICE);
        }

        Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), range, materials);

        if (block == null || !materials.contains(block.getType()) || !isBendableTempBlock(block)) {
            return Optional.empty();
        }

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) return Optional.empty();
        if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) return Optional.empty();

        return Optional.of(block);
    }

    private static boolean isBendableTempBlock(Block block) {
        TempBlock tempBlock = Game.getTempBlockService().getTempBlock(block);

        if (tempBlock == null) return true;

        return PhaseChange.isFrozenBlock(tempBlock);
    }

    public static boolean hasFullBottle(User user) {
        return user.getInventory().containsAtLeast(Game.plugin.getBottleSnapshot(), 1);
    }

    public static boolean hasEmptyBottle(User user) {
        return user.getInventory().containsAtLeast(new ItemStack(Material.GLASS_BOTTLE), 1);
    }

    public static boolean fillBottle(User user) {
        if (!BottleReturn.config.enabled) return false;
        if (!hasEmptyBottle(user)) return false;

        user.getInventory().removeAmount(new ItemStack(Material.GLASS_BOTTLE), 1);
        return user.getInventory().addItem(Game.plugin.getBottleSnapshot());
    }

    public static boolean emptyBottle(User user) {
        if (!BottleReturn.config.enabled) return false;
        if (!hasFullBottle(user)) return false;
        if (Game.getAbilityInstanceManager().hasAbility(user, BottleReturn.class)) return false;

        if (user.getInventory().removeAmount(Game.plugin.getBottleSnapshot(), 1)) {
            return user.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
        }

        return false;
    }
}
