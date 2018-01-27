package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.ChatColor;

public class AddCommand implements CoreCommand {
    private String[] aliases = { "add", "a" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof User)) {
            sender.sendMessage(ChatColor.RED + "Only users can execute this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/bending add [element]");
            return true;
        }

        String elementName = args[1];
        Element element = Game.getElementRegistry().getElementByName(elementName);

        if (element == null) {
            sender.sendMessage(ChatColor.RED + "There is no element named " + elementName + ".");
            return true;
        }

        User user = (User)sender;

        if (user.getElements().contains(element)) {
            sender.sendMessage(ChatColor.RED + "User already has element " + element.toString() + ChatColor.RED + ".");
            return true;
        }

        user.addElement(element);

        if (user instanceof Player) {
            Game.getPlayerService().saveElements((Player)user);
        }

        sender.sendMessage(ChatColor.GOLD + "Added element " + element.toString() + ChatColor.GOLD + ".");

        return true;
    }

    @Override
    public String getDescription() {
        return "Add an element.";
    }

    @Override
    public String getPermission() {
        return "bending.command.add";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
