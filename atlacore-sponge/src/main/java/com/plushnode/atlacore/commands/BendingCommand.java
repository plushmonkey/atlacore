package com.plushnode.atlacore.commands;

import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.command.CommandRegistry;
import com.plushnode.atlacore.command.CommandSender;
import com.plushnode.atlacore.command.CoreCommand;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class BendingCommand extends CommandRegistry implements CommandCallable {
    private static final String COMMAND_NAME = "bending";

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        // Note: this doesn't handle quoted strings, but it shouldn't be needed.
        String[] args = arguments.split(" ");

        if (args.length == 0) {
            sendUsage(source);
            return CommandResult.success();
        }

        String targetCommand = args[0].toLowerCase();
        CoreCommand bendingCommand = getCommands().get(targetCommand);

        if (bendingCommand == null) {
            sendUsage(source);
            return CommandResult.success();
        }

        String permission = bendingCommand.getPermission();

        if (permission != null && !source.hasPermission(permission)) {
            source.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this command."));
            return CommandResult.success();
        }

        if (source instanceof Player) {
            com.plushnode.atlacore.entity.user.Player player = Game.getPlayerService().getPlayerByName(source.getName());
            bendingCommand.execute(player, args);
        } else {
            // todo: handle console
            //bendingCommand.execute(sender, args);
        }

        return CommandResult.success();
    }

    private void sendUsage(CommandSource commandSender) {
        Set<CoreCommand> commandSet = new HashSet<>();
        commandSet.addAll(getCommands().values());

        for (CoreCommand command : commandSet) {
            String[] aliases = command.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;

            String name = aliases[0];

            Text usage = Text.of(TextColors.GREEN, "/" + COMMAND_NAME + " " + name, TextColors.GOLD, ": " + command.getDescription());

            commandSender.sendMessage(usage);
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("desc"));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("help"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("usage");
    }
}
