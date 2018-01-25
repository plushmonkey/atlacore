package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.platform.BlockWrapper;
import com.plushnode.atlacore.platform.EntityWrapper;
import com.plushnode.atlacore.platform.LocationWrapper;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BukkitCollisionSystem extends BaseCollisionSystem {
    @Override
    public boolean isOnGround(Entity entity) {
        final double epsilon = 0.01;

        org.bukkit.entity.Entity bEntity = ((EntityWrapper)entity).getBukkitEntity();

        Location location = bEntity.getLocation();
        AABB entityBounds = getAABB(bEntity).at(new LocationWrapper(location).subtract(0, epsilon, 0));

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                org.bukkit.block.Block checkBlock = location.clone().add(x, -epsilon, z).getBlock();
                if (checkBlock.getType() == org.bukkit.Material.AIR) continue;

                LocationWrapper wrapper = new LocationWrapper(checkBlock.getLocation());
                AABB checkBounds = getAABB(checkBlock).at(wrapper);

                if (entityBounds.intersects(checkBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public double distanceAboveGround(Entity entity) {
        return distanceAboveGround(entity, Collections.emptySet());
    }

    @Override
    public double distanceAboveGround(Entity entity, Set<Material> groundMaterials) {
        org.bukkit.entity.Entity bEntity = ((EntityWrapper)entity).getBukkitEntity();
        Set<org.bukkit.Material> bMaterials = groundMaterials.stream().map(TypeUtil::adapt).collect(Collectors.toSet());

        Location location = bEntity.getLocation().clone();
        Ray ray = new Ray(new LocationWrapper(location), new Vector3D(0, -1, 0));

        for (double y = location.getY() - 1; y >= 0; --y) {
            location.setY(y);

            org.bukkit.block.Block block = location.getBlock();
            AABB checkBounds;

            if (bMaterials.contains(block.getType())) {
                checkBounds = AABB.BLOCK_BOUNDS;
            } else {
                checkBounds = getAABB(block);
            }

            checkBounds = checkBounds.at(new LocationWrapper(block.getLocation()));

            Optional<Double> rayHit = checkBounds.intersects(ray);

            if (rayHit.isPresent()) {
                return rayHit.get();
            }
        }

        return Double.MAX_VALUE;
    }

    @Override
    public AABB getAABB(Entity entity) {
        return BukkitAABB.getEntityBounds(((EntityWrapper)entity).getBukkitEntity());
    }

    @Override
    public AABB getAABB(Block block) {
        return BukkitAABB.getBlockBounds(((BlockWrapper)block).getBukkitBlock());
    }

    private AABB getAABB(org.bukkit.block.Block block) {
        return BukkitAABB.getBlockBounds(block);
    }

    private AABB getAABB(org.bukkit.entity.Entity entity) {
        return BukkitAABB.getEntityBounds(entity);
    }
}
