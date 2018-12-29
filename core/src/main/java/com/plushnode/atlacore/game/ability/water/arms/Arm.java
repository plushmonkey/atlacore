package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.Levelled;
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
    private boolean full;
    private List<TempBlock> tempBlocks = new ArrayList<>();

    public Arm(User user, Vector3D offset, int length) {
        this.user = user;
        this.offset = offset;
        this.length = length;
        this.material = Material.WATER;
        this.full = false;
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

        this.full = true;

        for (int i = 0; i < getLength(); ++i) {
            Location current = base.add(user.getDirection().scalarMultiply(i));
            Block block = current.getBlock();

            if (MaterialUtil.isTransparent(block)) {
                tempBlocks.add(new TempBlock(block, material));
            } else if (block.getType() != Material.WATER) {
                this.full = false;
                break;
            }
        }
    }

    public int getLength() {
        return length;
    }

    public boolean isFull() {
        return full;
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
        return isActive() && this.state.canActivate();
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
        default boolean canActivate() {
            return false;
        }
    }

    public static class DefaultArmState implements ArmState {
        @Override
        public boolean update() {
            return true;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean canActivate() {
            return true;
        }
    }

    public abstract static class ExtensionArmState implements ArmState {
        protected Arm arm;
        protected int extension;
        protected double speed;
        protected double configSpeed;
        protected int configLength;
        protected List<TempBlock> tempBlocks = new ArrayList<>();

        ExtensionArmState(Arm arm, double speed, int length) {
            this.arm = arm;
            this.extension = 0;
            this.speed = speed;
            this.configSpeed = speed;
            this.configLength = length;
        }

        @Override
        public boolean update() {
            Location begin = arm.getEnd().subtract(arm.getUser().getDirection()).getBlock().getLocation().add(0.5, 0.5, 0.5);

            Location target = arm.getUser().getEyeLocation().add(arm.getUser().getDirection().scalarMultiply(arm.getLength() + configLength - 1));
            target = target.getBlock().getLocation().add(0.5, 0.5, 0.5);
            Vector3D direction = target.subtract(begin).toVector().normalize();

            this.extension += speed;

            clear();

            if (this.extension >= configLength) {
                this.extension = configLength;
                speed = -configSpeed;
            }

            Location end = arm.getEnd();
            for (int i = 0; i < Math.ceil(extension); ++i) {
                Location current = end.add(direction.scalarMultiply(i));
                Block block = current.getBlock();
                if (block.getType() == Material.WATER) continue;

                if (!MaterialUtil.isTransparent(block)) {
                    speed = -configSpeed;
                    onBlockCollision(block);
                    break;
                }

                int level = (int)((i / Math.ceil(extension)) * 3);

                tempBlocks.add(new TempBlock(block, Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(level))));
            }

            Location current = begin.add(direction.scalarMultiply(extension)).getBlock().getLocation().add(0.5, 0.5, 0.5);
            act(current);

            // Set it back to default state once the arm retracts.
            if (this.extension <= 0) {
                clear();
                arm.setState(new Arm.DefaultArmState());
            }

            return true;
        }

        @Override
        public void clear() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();
        }

        public void onBlockCollision(Block block) {

        }

        public abstract void act(Location location);
    }
}
