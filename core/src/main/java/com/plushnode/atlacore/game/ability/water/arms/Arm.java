package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.block.TempBlock;
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

    public Arm(User user, Vector3D offset) {
        this.user = user;
        this.offset = offset;

        this.state = new DefaultArmState(this);
    }

    public User getUser() {
        return user;
    }

    public void clear() {
        if (state != null) {
            state.clear();
        }
    }

    public boolean update() {
        clear();

        if (!isActive()) return false;

        return state.update();
    }

    public Location getBase() {
        Vector3D worldOffset = VectorUtil.rotate(offset, Vector3D.PLUS_J, Math.toRadians(-user.getYaw()));
        return user.getEyeLocation().add(worldOffset);
    }

    public boolean isActive() {
        return state != null;
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
        private Arm arm;
        private List<TempBlock> tempBlocks = new ArrayList<>();

        public DefaultArmState(Arm arm) {
            this.arm = arm;
        }

        @Override
        public boolean update() {
            Location base = arm.getBase();

            for (int i = 0; i < 5; ++i) {
                Location current = base.add(arm.user.getDirection().scalarMultiply(i));
                Block block = current.getBlock();

                if (MaterialUtil.isTransparent(block)) {
                    tempBlocks.add(new TempBlock(block, Material.WATER));
                }
            }

            return true;
        }

        @Override
        public void clear() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();
        }
    }
}
