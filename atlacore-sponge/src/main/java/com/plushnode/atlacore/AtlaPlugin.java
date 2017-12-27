package com.plushnode.atlacore;

import com.google.inject.Inject;
import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.block.setters.BlockSetterFactory;
import com.plushnode.atlacore.collision.SpongeCollisionSystem;
import com.plushnode.atlacore.commands.HelloWorldCommand;
import com.plushnode.atlacore.config.Configuration;
import com.plushnode.atlacore.listeners.PlayerListener;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "atlacore")
public class AtlaPlugin implements CorePlugin {
    public static AtlaPlugin plugin;
    public static com.plushnode.atlacore.Game atlaGame;

    private BlockSetterFactory blockSetterFactory = new BlockSetterFactory();
    private SpongeParticleEffectRenderer particleEffectRenderer = new SpongeParticleEffectRenderer();

    @Inject
    private Game game;
    @Inject
    private Logger log;

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        plugin = this;

        atlaGame = new com.plushnode.atlacore.Game(this, new SpongeCollisionSystem());
        com.plushnode.atlacore.Game.getProtectionSystem().reload();

        CommandSpec testCommand = CommandSpec.builder()
                .description(Text.of("Hello"))
                .permission("atlacore.command.hello")
                .executor(new HelloWorldCommand())
                .build();

        Sponge.getCommandManager().register(this, testCommand, "helloworld", "hello", "test");

        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));

        createTaskTimer(atlaGame::update, 1, 1);
    }

    public com.plushnode.atlacore.Game getAtlaGame() {
        return atlaGame;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        log.info("Test!");
    }

    public Logger getLogger() {
        return this.log;
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
