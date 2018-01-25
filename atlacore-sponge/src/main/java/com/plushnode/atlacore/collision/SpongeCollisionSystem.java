package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import com.plushnode.atlacore.platform.EntityWrapper;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

// todo: merge common stuff with bukkit collision system
public class SpongeCollisionSystem extends BaseCollisionSystem {
    @Override
    public boolean isOnGround(Entity entity) {
        final double epsilon = 0.01;
        AABB entityBounds = getAABB(entity).at(entity.getLocation().subtract(0, epsilon, 0));

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                Block checkBlock = entity.getLocation().add(x, -epsilon, z).getBlock();

                if (checkBlock.getType() == Material.AIR) continue;

                AABB blockBounds = getAABB(checkBlock).at(checkBlock.getLocation());

                if (entityBounds.intersects(blockBounds)) {
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
        Location location = entity.getLocation();
        Ray ray = new Ray(location, new Vector3D(0, -1, 0));

        for (double y = location.getY(); y >= 0; --y) {
            Location checkLocation = location.setY(y);
            Block block = checkLocation.getBlock();

            AABB checkBounds = AABB.BLOCK_BOUNDS;

            if (!groundMaterials.contains(block.getType())) {
                checkBounds = getAABB(block);
            }

            checkBounds = checkBounds.at(block.getLocation());
            Optional<Double> rayHit = checkBounds.intersects(ray);
            if (rayHit.isPresent()) {
                return rayHit.get();
            }
        }

        return Double.MAX_VALUE;
    }

    @Override
    public AABB getAABB(Entity entity) {
        org.spongepowered.api.entity.Entity spongeEntity = ((EntityWrapper)entity).getSpongeEntity();
        Optional<org.spongepowered.api.util.AABB> result = spongeEntity.getBoundingBox();
        if (result.isPresent()) {
            Vector3D min = SpongeTypeUtil.adapt(result.get().getMin()).subtract(entity.getLocation().toVector());
            Vector3D max = SpongeTypeUtil.adapt(result.get().getMax()).subtract(entity.getLocation().toVector());
            return new AABB(min, max);
        }

        return new AABB(null, null);
    }

    @Override
    public AABB getAABB(Block block) {
        // todo: get actual block bounding box instead of binary solid
        if (MaterialUtil.isTransparent(block)) {
            return new AABB(null, null);
        }

        return new AABB(new Vector3D(0, 0, 0), new Vector3D(1, 1, 1));
    }
}
