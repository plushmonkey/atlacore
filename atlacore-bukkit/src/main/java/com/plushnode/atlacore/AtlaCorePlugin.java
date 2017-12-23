package com.plushnode.atlacore;

import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.block.setters.StandardBlockSetter;
import com.plushnode.atlacore.collision.BukkitCollisionSystem;
import com.plushnode.atlacore.config.Configuration;
import com.plushnode.atlacore.listeners.PlayerListener;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectionSystem;
import com.plushnode.atlacore.protection.methods.*;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.LocationWrapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AtlaCorePlugin extends JavaPlugin implements CorePlugin {
    public static AtlaCorePlugin plugin;
    private BlockSetterFactory blockSetterFactory;
    private BukkitParticleEffectRenderer particleRenderer = new BukkitParticleEffectRenderer();
    private Game game;

    @Override
    public void onEnable() {
        plugin = this;

        this.game = new Game(this, new BukkitCollisionSystem());
        this.blockSetterFactory = new BlockSetterFactory();

        ProtectionSystem protection = Game.getProtectionSystem();

        protection.getFactory().registerProtectMethod("Factions", () -> new FactionsProtectMethod(this));
        protection.getFactory().registerProtectMethod("GriefPrevention", () -> new GriefPreventionProtectMethod(this));
        protection.getFactory().registerProtectMethod("LWC", () -> new LWCProtectMethod(this));
        protection.getFactory().registerProtectMethod("Towny", () -> new TownyProtectMethod(this));
        protection.getFactory().registerProtectMethod("WorldGuard", () -> new WorldGuardProtectMethod(this));

        // TODO: fix this
        // Must be called after registering all protection methods.
        protection.reload();

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

    @Override
    public Task createTask(Task task, long delay) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        runnable.runTaskLater(this, delay);

        return new Task() {
            @Override
            public void run() {

            }

            @Override
            public void cancel() {
                runnable.cancel();
            }
        };
    }

    @Override
    public Task createTaskTimer(Task task, long delay, long period) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        runnable.runTaskTimer(this, delay, period);

        return new Task() {
            @Override
            public void run() {

            }

            @Override
            public void cancel() {
                runnable.cancel();
            }
        };
    }

    @Override
    public ParticleEffectRenderer getParticleRenderer() {
        return particleRenderer;
    }
}
