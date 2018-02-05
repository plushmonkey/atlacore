package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.ChatColor;

public class ChooseCommand implements CoreCommand {
    private String[] aliases = { "choose", "ch" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof User)) {
            sender.sendMessage(ChatColor.RED + "Only users can execute this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/bending choose [element]");
            return true;
        }

        String elementName = args[1];
        Element element = Game.getElementRegistry().getElementByName(elementName);

        if (element == null) {
            sender.sendMessage(ChatColor.RED + "There is no element named " + elementName + ".");
            return true;
        }

        User user = (User)sender;

        user.getElements().clear();
        user.addElement(element);
        user.validateSlots();

        if (user instanceof Player) {
            Game.getPlayerService().saveElements((Player)user);
            Game.getPlayerService().saveSlots((Player)user);
        }

        Game.getAbilityInstanceManager().clearPassives(user);
        Game.getAbilityInstanceManager().createPassives(user);

        sender.sendMessage(ChatColor.GOLD + "Element set to " + element.toString() + ChatColor.GOLD + ".");

        return true;
    }

    @Override
    public String getDescription() {
        return "Choose an element.";
    }

    @Override
    public String getPermission() {
        return "bending.command.choose";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
