package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.User;

public class ChooseCommand implements CoreCommand {
    private String[] aliases = { "choose", "ch" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof User)) {
            sender.sendMessage("Only users can execute this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("/bending choose [element]");
            return true;
        }

        String elementName = args[1];
        Element element = Game.getElementRegistry().getElementByName(elementName);

        if (element == null) {
            sender.sendMessage("There is no element named " + elementName + ".");
            return true;
        }

        User user = (User)sender;

        user.getElements().clear();
        user.addElement(element);
        user.validateSlots();

        sender.sendMessage("Element set to " + element.getName() + ".");

        return true;
    }

    @Override
    public String getDescription() {
        return "Choose an element.";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
