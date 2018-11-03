package com.plushnode.atlacore.commands;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.command.CommandRegistry;
import com.plushnode.atlacore.command.CoreCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CoreExecutor extends CommandRegistry implements CommandExecutor {
    private static final String COMMAND_NAME = "atla";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String targetCommand = args[0].toLowerCase();
        CoreCommand bendingCommand = getCommands().get(targetCommand);

        if (bendingCommand == null) {
            sendUsage(sender);
            return true;
        }

        String permission = bendingCommand.getPermission();

        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (sender instanceof Player) {
            com.plushnode.atlacore.platform.Player player = Game.getPlayerService().getPlayerByName(sender.getName());
            bendingCommand.execute(player, args);
        } else {
            // todo: handle console
            //bendingCommand.execute(sender, args);
        }

        return true;
    }

    private void sendUsage(CommandSender commandSender) {
        List<CoreCommand> commands = new ArrayList<>(new HashSet<>(getCommands().values()));
        commands.sort(Comparator.comparing(command -> command.getAliases()[0]));

        boolean sentCommand = false;

        for (CoreCommand command : commands) {
            String[] aliases = command.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;

            // Hide any commands that the sender doesn't have permission for.
            if (!commandSender.hasPermission(command.getPermission())) continue;

            String name = aliases[0];
            String usage = ChatColor.GREEN + "/" + COMMAND_NAME + " " + name + ChatColor.GOLD + ": " + command.getDescription();

            commandSender.sendMessage(usage);
            sentCommand = true;
        }

        if (!sentCommand) {
            String noCommands = ChatColor.RED + "You don't have permission to use any bending commands.";
            commandSender.sendMessage(noCommands);
        }
    }
}
