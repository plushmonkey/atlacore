package com.plushnode.atlacore;

import com.google.inject.Inject;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.collision.SpongeCollisionSystem;
import com.plushnode.atlacore.commands.HelloWorldCommand;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.listeners.PlayerListener;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "atlacore")
public class AtlaPlugin implements CorePlugin {
    public static AtlaPlugin plugin;
    public static com.plushnode.atlacore.Game atlaGame;

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

        atlaGame = new com.plushnode.atlacore.Game(this, new SpongeCollisionSystem());

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

    public com.plushnode.atlacore.Game getAtlaGame() {
        return atlaGame;
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        CommandSpec testCommand = CommandSpec.builder()
                .description(Text.of("Hello"))
                .permission("atlacore.command.hello")
                .executor(new HelloWorldCommand())
                .build();

        Sponge.getCommandManager().register(this, testCommand, "helloworld", "hello", "test");

        com.plushnode.atlacore.Game.getProtectionSystem().reload();
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
