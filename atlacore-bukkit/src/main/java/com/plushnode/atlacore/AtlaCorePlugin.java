package com.plushnode.atlacore;

import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.config.Configuration;
import com.plushnode.atlacore.listeners.PlayerListener;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.LocationWrapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AtlaCorePlugin extends JavaPlugin implements CorePlugin {
    public static AtlaCorePlugin plugin;
    private BlockSetter normalBlockSetter;
    private Game game;

    @Override
    public void onEnable() {
        plugin = this;

        this.game = new Game(this);

        getLogger().info("Enabling AltaCore-Bukkit");

        normalBlockSetter = new BlockSetter() {
            @Override
            public void setBlock(Location location, int typeId, byte data) {
                LocationWrapper wrapper = (LocationWrapper)location;
                wrapper.getBukkitLocation().getBlock().setTypeIdAndData(typeId, data, true);
            }

            @Override
            public void setBlock(Location location, Material material) {
                LocationWrapper wrapper = (LocationWrapper)location;
                wrapper.getBukkitLocation().getBlock().setType(TypeUtil.adapt(material));
            }

            @Override
            public void setBlock(Block block, int typeId, byte data) {
                setBlock(block.getLocation(), typeId, data);
            }

            @Override
            public void setBlock(Block block, Material material) {
                setBlock(block.getLocation(), material);
            }
        };

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                game.update();
            }
        }.runTaskTimer(this, 1, 1);
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void onConfigReload(Configuration config) {

    }

    @Override
    public BlockSetter getBlockSetter() {
        return normalBlockSetter;
    }

    @Override
    public BlockSetter getBlockSetter(BlockSetter.Flag... flags) {
        for (BlockSetter.Flag flag : flags) {
            if (flag == BlockSetter.Flag.LIGHTING) {
                return normalBlockSetter;
            }
        }

        // todo: native block setter that bypasses lighting
        return normalBlockSetter;
    }
}
