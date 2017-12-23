package com.plushnode.atlacore;

import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.block.setters.StandardBlockSetter;
import com.plushnode.atlacore.config.Configuration;
import com.plushnode.atlacore.listeners.PlayerListener;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.LocationWrapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AtlaCorePlugin extends JavaPlugin implements CorePlugin {
    public static AtlaCorePlugin plugin;
    private BlockSetterFactory blockSetterFactory;
    private Game game;

    @Override
    public void onEnable() {
        plugin = this;

        this.game = new Game(this);
        this.blockSetterFactory = new BlockSetterFactory();

        getLogger().info("Enabling AltaCore-Bukkit");
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
        return blockSetterFactory.getBlockSetter();
    }

    @Override
    public BlockSetter getBlockSetter(BlockSetter.Flag... flags) {
        return blockSetterFactory.getBlockSetter(flags);
    }
}
