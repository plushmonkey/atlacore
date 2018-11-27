package com.plushnode.atlacore.game.ability.common.source;

import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.game.Game;
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

        if (block == null || !materials.contains(block.getType()) || Game.getTempBlockService().isTempBlock(block)) {
            return Optional.empty();
        }

        return Optional.of(block);
    }
}
