package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;

import java.util.Arrays;
import java.util.Optional;

public final class CollisionUtil {
    private CollisionUtil() {

    }

    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly) {
        return handleEntityCollisions(user, collider, callback, livingOnly, false);
    }

    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(User user, Collider collider, CollisionCallback callback, boolean livingOnly, boolean selfCollision) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector3D extent = collider.getHalfExtents().add(new Vector3D(ExtentBuffer, ExtentBuffer, ExtentBuffer));
        Vector3D pos = collider.getPosition();
        Location location = user.getLocation().setX(pos.getX()).setY(pos.getY()).setZ(pos.getZ());

        boolean hit = false;

        for (Entity entity : location.getWorld().getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (!selfCollision && entity.equals(user)) continue;

            if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            if (livingOnly && !(entity instanceof LivingEntity)) {
                continue;
            }

            if (livingOnly) {
                if (!((LivingEntity)entity).isVisible()) continue;
            }

            AABB entityBounds = entity.getBounds().at(entity.getLocation());
            if (collider.intersects(entityBounds)) {
                if (callback.onCollision(entity)) {
                    return true;
                }

                hit = true;
            }
        }

        return hit;
    }

    public static Pair<Boolean, Location> handleBlockCollisions(Collider collider, Location begin, Location end, boolean liquids) {
        if (end.equals(begin)) {
            return new Pair<>(false, null);
        }

        double maxExtent = VectorUtil.getMaxComponent(collider.getHalfExtents());
        double distance = begin.distance(end);

        Vector3D toEnd = end.subtract(begin).toVector().normalize();
        Ray ray = new Ray(begin.toVector(), toEnd);

        Location mid = begin.add(toEnd.scalarMultiply(distance / 2.0));
        double lookupRadius = (distance / 2.0) + maxExtent + 1.0;

        for (Block block : WorldUtil.getNearbyBlocks(mid, lookupRadius, Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR))) {
            AABB localBounds = block.getBounds();

            if (liquids && block.isLiquid()) {
                localBounds = AABB.BLOCK_BOUNDS;
            }

            AABB blockBounds = localBounds.at(block.getLocation());

            Optional<Double> result = blockBounds.intersects(ray);
            if (result.isPresent()) {
                double d = result.get();
                if (d < distance) {
                    return new Pair<>(true, begin.add(toEnd.scalarMultiply(d)));
                }
            }
        }

        return new Pair<>(false, null);
    }

    public static Optional<Pair<Double, Double>> sweepAABB(AABB a, Vector3D aPrevPos, Vector3D aCurPos, AABB b, Vector3D bPrevPos, Vector3D bCurPos) {
        AABB aPrev = a.at(aPrevPos);
        AABB bPrev = b.at(bPrevPos);

        Vector3D da = aCurPos.subtract(aPrevPos);
        Vector3D db = bCurPos.subtract(bPrevPos);

        Vector3D v = db.subtract(da);

        Vector3D overlapFirst = Vector3D.ZERO;
        Vector3D overlapLast = Vector3D.ZERO;

        if (aPrev.intersects(bPrev)) {
            return Optional.of(new Pair<>(0.0, 0.0));
        }

        for (int i = 0; i < 3; ++i) {
            double aMax = VectorUtil.component(a.max(), i);
            double aMin = VectorUtil.component(a.min(), i);
            double bMax = VectorUtil.component(b.max(), i);
            double bMin = VectorUtil.component(b.min(), i);
            double vi = VectorUtil.component(v, i);

            if (aMax < bMin && vi < 0) {
                VectorUtil.setComponent(overlapFirst, i, (aMax - bMin) / vi);
            } else if (bMax < aMin && vi > 0) {
                VectorUtil.setComponent(overlapFirst, i, (aMin - bMax) / vi);
            } else if (bMax > aMin && vi < 0) {
                VectorUtil.setComponent(overlapLast, i, (aMin - bMax) / vi);
            } else if (aMax > bMin && vi > 0) {
                VectorUtil.setComponent(overlapLast, i, (aMax - bMin) / vi);
            }
        }

        double t1 = VectorUtil.getMaxComponent(overlapFirst);
        double t2 = VectorUtil.getMinComponent(overlapLast);

        if (t1 <= t2) {
            return Optional.of(new Pair<>(t1, t2));
        }

        return Optional.empty();
    }
}
