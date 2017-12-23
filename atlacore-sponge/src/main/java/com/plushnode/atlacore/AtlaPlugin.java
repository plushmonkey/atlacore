package com.plushnode.atlacore;

import com.google.inject.Inject;
import com.plushnode.atlacore.commands.HelloWorldCommand;
import com.plushnode.atlacore.listeners.HelloListener;
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
public class AtlaPlugin {
    @Inject
    private Game game;
    @Inject
    private Logger log;

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        CommandSpec testCommand = CommandSpec.builder()
                .description(Text.of("Hello"))
                .permission("atlacore.command.hello")
                .executor(new HelloWorldCommand())
                .build();

        Sponge.getCommandManager().register(this, testCommand, "helloworld", "hello", "test");

        Sponge.getEventManager().registerListeners(this, new HelloListener(log));
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        log.info("Test!");
    }

    public Logger getLogger() {
        return this.log;
    }
}
