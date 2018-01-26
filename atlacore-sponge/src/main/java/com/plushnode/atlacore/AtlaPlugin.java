package com.plushnode.atlacore;

import com.google.inject.Inject;
import com.plushnode.atlacore.command.AddCommand;
import com.plushnode.atlacore.command.ChooseCommand;
import com.plushnode.atlacore.events.SneakEventDispatcher;
import com.plushnode.atlacore.platform.SpongeBendingPlayer;
import com.plushnode.atlacore.platform.SpongeParticleEffectRenderer;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.collision.SpongeCollisionSystem;
import com.plushnode.atlacore.command.BindCommand;
import com.plushnode.atlacore.commands.BendingCommand;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.listeners.PlayerListener;
import com.plushnode.atlacore.platform.ParticleEffectRenderer;
import com.plushnode.atlacore.player.MemoryPlayerRepository;
import com.plushnode.atlacore.player.PlayerFactory;
import com.plushnode.atlacore.player.PlayerRepository;
import com.plushnode.atlacore.player.PlayerService;
import com.plushnode.atlacore.util.Task;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Plugin(id = "atlacore-sponge")
public class AtlaPlugin implements CorePlugin {
    public static AtlaPlugin plugin;
    public static com.plushnode.atlacore.game.Game atlaGame;

    private BlockSetterFactory blockSetterFactory = new BlockSetterFactory();
    private SpongeParticleEffectRenderer particleEffectRenderer = new SpongeParticleEffectRenderer();
    private SneakEventDispatcher sneakDispatcher;

    @Inject
    private Game game;
    @Inject
    private Logger log;
    @Inject
    @DefaultConfig(sharedRoot=false)
    private Path configPath;
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> loader;


    @Listener
    public void onServerInit(GameInitializationEvent event) {
        plugin = this;

        loadConfig();

        PlayerRepository playerRepository = new MemoryPlayerRepository(new PlayerFactory() {
            @Override
            public Player createPlayer(String name) {
                Optional<org.spongepowered.api.entity.living.player.Player> result = Sponge.getServer().getPlayer(name);
                if (!result.isPresent()) {
                    return null;
                }

                return new SpongeBendingPlayer(result.get());
            }

            @Override
            public Player createPlayer(UUID uuid) {
                Optional<org.spongepowered.api.entity.living.player.Player> result = Sponge.getServer().getPlayer(uuid);
                if (!result.isPresent()) {
                    return null;
                }

                return new SpongeBendingPlayer(result.get());
            }
        });

        PlayerService playerService = new PlayerService(playerRepository);

        atlaGame = new com.plushnode.atlacore.game.Game(this, new SpongeCollisionSystem(), playerService);

        createTaskTimer(atlaGame::update, 1, 1);

        sneakDispatcher = new SneakEventDispatcher();
        createTaskTimer(sneakDispatcher::run, 1, 1);

        Sponge.getEventManager().registerListeners(this, sneakDispatcher);
        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));

        // Save the config after loading everything so the defaults are saved.
        try {
            loader.save(configRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public com.plushnode.atlacore.game.Game getAtlaGame() {
        return atlaGame;
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        BendingCommand cmd = new BendingCommand();

        cmd.registerCommand(new BindCommand());
        cmd.registerCommand(new ChooseCommand());
        cmd.registerCommand(new AddCommand());

        Sponge.getCommandManager().register(this, cmd, "bending", "b", "atla");

        com.plushnode.atlacore.game.Game.getProtectionSystem().reload();
    }

    private void loadConfig() {
        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        loader = HoconConfigurationLoader.builder()
                .setPath(configPath)
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

    public Logger getLogger() {
        return this.log;
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
        org.spongepowered.api.scheduler.Task spongeTask = Sponge.getScheduler()
                .createTaskBuilder()
                .delayTicks(delay)
                .execute(task::run)
                .submit(this);


        return new Task() {
            @Override
            public void run() {

            }

            @Override
            public void cancel() {
                spongeTask.cancel();
            }
        };
    }

    @Override
    public Task createTaskTimer(Task task, long delay, long period) {
        org.spongepowered.api.scheduler.Task spongeTask = Sponge.getScheduler()
                .createTaskBuilder()
                .delayTicks(delay)
                .intervalTicks(period)
                .execute(task::run)
                .submit(this);

        return new Task() {
            @Override
            public void run() {

            }

            @Override
            public void cancel() {
                spongeTask.cancel();
            }
        };
    }

    @Override
    public ParticleEffectRenderer getParticleRenderer() {
        return particleEffectRenderer;
    }
}
