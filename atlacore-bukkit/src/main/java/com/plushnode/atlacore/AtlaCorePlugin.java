package com.plushnode.atlacore;

import com.plushnode.atlacore.command.AddCommand;
import com.plushnode.atlacore.command.ChooseCommand;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.BukkitParticleEffectRenderer;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.collision.BukkitCollisionSystem;
import com.plushnode.atlacore.command.BindCommand;
import com.plushnode.atlacore.commands.CoreExecutor;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.listeners.PlayerListener;
import com.plushnode.atlacore.platform.ParticleEffectRenderer;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.player.MemoryPlayerRepository;
import com.plushnode.atlacore.player.PlayerFactory;
import com.plushnode.atlacore.player.PlayerRepository;
import com.plushnode.atlacore.player.PlayerService;
import com.plushnode.atlacore.protection.ProtectionSystem;
import com.plushnode.atlacore.protection.methods.*;
import com.plushnode.atlacore.util.Task;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class AtlaCorePlugin extends JavaPlugin implements CorePlugin {
    public static AtlaCorePlugin plugin;

    private CommentedConfigurationNode configRoot;
    private BlockSetterFactory blockSetterFactory;
    private BukkitParticleEffectRenderer particleRenderer = new BukkitParticleEffectRenderer();
    private Game game;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Override
    public void onEnable() {
        plugin = this;

        loadConfig();

        PlayerRepository playerRepository = new MemoryPlayerRepository(new PlayerFactory() {
            @Override
            public Player createPlayer(String name) {
                if (Bukkit.getPlayer(name) == null)
                    return null;
                return new BukkitBendingPlayer(Bukkit.getPlayer(name));
            }

            @Override
            public Player createPlayer(UUID uuid) {
                if (Bukkit.getPlayer(uuid) == null)
                    return null;
                return new BukkitBendingPlayer(Bukkit.getPlayer(uuid));
            }
        });

        PlayerService playerService = new PlayerService(playerRepository);

        this.game = new Game(this, new BukkitCollisionSystem(), playerService);
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

        CoreExecutor executor = new CoreExecutor();

        executor.registerCommand(new BindCommand());
        executor.registerCommand(new ChooseCommand());
        executor.registerCommand(new AddCommand());

        this.getCommand("b").setExecutor(executor);
        this.getCommand("atla").setExecutor(executor);
        this.getCommand("bending").setExecutor(executor);

        // Save the config after loading everything so the defaults are saved.
        try {
            loader.save(configRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                game.update();
            }
        }.runTaskTimer(this, 1, 1);
    }

    private void loadConfig() {
        getLogger().info("Loading config.");
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                getLogger().warning("Failed to create data folder.");
            }
        }


        File configFile = new File(dataFolder.getPath() + "/atlacore.conf");
        Path path = configFile.toPath();

        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        loader = HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(options)
                .build();

        try {
            configRoot = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigManager.getInstance().setConfig(configRoot);
        ConfigManager.getInstance().onConfigReload();
    }

    public Game getGame() {
        return game;
    }

    @Override
    public CommentedConfigurationNode getCoreConfig() {
        return configRoot;
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
