package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.ChatColor;

public class ToggleCommand implements CoreCommand {
    private String[] aliases = { "toggle", "t" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof User)) {
            sender.sendMessage(ChatColor.RED + "Only users can execute this command.");
            return true;
        }

        User user = (User)sender;

        if (user.getBendingConditional().hasType(ToggledConditional.class)) {
            user.getBendingConditional().removeType(ToggledConditional.class);

            sender.sendMessage(ChatColor.GOLD + "Your bending has been toggled back on.");
        } else {
            user.getBendingConditional().add(new ToggledConditional());

            sender.sendMessage(ChatColor.GOLD + "Your bending has been toggled off.");
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Toggles bending on/off.";
    }

    @Override
    public String getPermission() {
        return "atla.command.toggle";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }

    private static class ToggledConditional implements BendingConditional {
        @Override
        public boolean canBend(User user, AbilityDescription desc) {
            return false;
        }
    }
}
