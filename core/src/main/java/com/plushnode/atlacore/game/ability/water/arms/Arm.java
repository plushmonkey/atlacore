package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class Arm {
    private User user;
    private Vector3D offset;
    private ArmState state;
    private int length;
    private Material material;
    private List<TempBlock> tempBlocks = new ArrayList<>();

    public Arm(User user, Vector3D offset, int length) {
        this.user = user;
        this.offset = offset;
        this.length = length;
        this.material = Material.WATER;

        this.state = new DefaultArmState();
    }

    public User getUser() {
        return user;
    }

    public void clear() {
        tempBlocks.forEach(TempBlock::reset);
        tempBlocks.clear();

        if (state != null) {
            state.clear();
        }
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public boolean update() {
        clear();

        if (!isActive()) return false;

        render();

        return state.update();
    }

    public void render() {
        Location base = getBase().getBlock().getLocation().add(0.5, 0.5, 0.5);

        for (Block block : RayCaster.blockArray(user.getWorld(), new Ray(base, user.getDirection()), getLength())) {
            if (MaterialUtil.isTransparent(block)) {
                tempBlocks.add(new TempBlock(block, material));
            }
        }
    }

    public int getLength() {
        return length;
    }

    public Vector3D getDirection() {
        return user.getDirection();
    }

    public Location getBase() {
        Vector3D worldOffset = VectorUtil.rotate(offset, Vector3D.PLUS_J, Math.toRadians(-user.getYaw()));
        return user.getEyeLocation().add(worldOffset);
    }

    public Location getEnd() {
        return getBase().add(user.getDirection().scalarMultiply(length));
    }

    public boolean isActive() {
        return state != null;
    }

    public boolean canActivate() {
        return this.state instanceof DefaultArmState;
    }

    public void setState(ArmState state) {
        if (this.state != null) {
            this.state.clear();
        }
        this.state = state;
    }

    public interface ArmState {
        boolean update();
        void clear();
    }

    public static class DefaultArmState implements ArmState {
        @Override
        public boolean update() {
            return true;
        }

        @Override
        public void clear() {

        }
    }
}
