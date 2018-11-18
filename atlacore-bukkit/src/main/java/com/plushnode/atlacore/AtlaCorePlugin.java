package com.plushnode.atlacore;

import com.plushnode.atlacore.command.*;
import com.plushnode.atlacore.event.EventBus;
import com.plushnode.atlacore.events.BendingEventBus;
import com.plushnode.atlacore.events.armor.ArmorListener;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.listeners.BlockListener;
import com.plushnode.atlacore.listeners.TempArmorListener;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.commands.CoreExecutor;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.listeners.PlayerListener;
import com.plushnode.atlacore.player.*;
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
import java.util.Collections;
import java.util.UUID;

public class AtlaCorePlugin extends JavaPlugin implements CorePlugin {
    public static AtlaCorePlugin plugin;

    private CommentedConfigurationNode configRoot;
    private BlockSetterFactory blockSetterFactory = new BlockSetterFactory();
    private BukkitParticleEffectRenderer particleRenderer = new BukkitParticleEffectRenderer();
    private BukkitPotionFactory potionFactory = new BukkitPotionFactory();
    private Game game;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private BendingEventBus eventBus = new BendingEventBus();

    @Override
    public void onEnable() {
        plugin = this;

        loadConfig();

        this.game = new Game(this);

        ProtectionSystem protection = Game.getProtectionSystem();

        protection.getFactory().registerProtectMethod("Factions", () -> new FactionsProtectMethod(this));
        protection.getFactory().registerProtectMethod("GriefPrevention", () -> new GriefPreventionProtectMethod(this));
        protection.getFactory().registerProtectMethod("LWC", () -> new LWCProtectMethod(this));
        protection.getFactory().registerProtectMethod("Towny", () -> new TownyProtectMethod(this));
        protection.getFactory().registerProtectMethod("WorldGuard", () -> new WorldGuardProtectMethod(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new TempArmorListener(), this);
        getServer().getPluginManager().registerEvents(new ArmorListener(Collections.emptyList()), this);

        CoreExecutor executor = new CoreExecutor();

        executor.registerCommand(new BindCommand());
        executor.registerCommand(new ChooseCommand());
        executor.registerCommand(new AddCommand());
        executor.registerCommand(new ReloadCommand());
        executor.registerCommand(new DisplayCommand());
        executor.registerCommand(new HelpCommand());
        executor.registerCommand(new PresetCommand());
        executor.registerCommand(new ModifyCommand());

        //this.getCommand("b").setExecutor(executor);
        this.getCommand("atla").setExecutor(executor);
        //this.getCommand("bending").setExecutor(executor);

        // Save the config after loading everything so the defaults are saved.
        try {
            loader.save(configRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reload config after Game was created so all of the values are set.
        loadConfig();
    }

    @Override
    public void onDisable() {
        Game.getTempArmorService().reload();
        Game.getTempBlockService().resetAll();
    }

    @Override
    public void loadConfig() {
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

    @Override
    public BukkitPotionFactory getPotionFactory() {
        return potionFactory;
    }

    @Override
    public PlayerFactory getPlayerFactory() {
        return new PlayerFactory() {
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
        };
    }

    @Override
    public String getConfigFolder() {
        return getDataFolder().toString();
    }

    @Override
    public void info(String message) {
        getLogger().info(message);
    }

    @Override
    public void warn(String message) {
        getLogger().warning(message);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
}
