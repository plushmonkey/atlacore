package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.util.ChatColor;

public class ReloadCommand implements CoreCommand {
    private String[] aliases = { "reload" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Game.reload();

        sender.sendMessage(ChatColor.GOLD + "Bending plugin config reloaded.");

        return true;
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin config.";
    }

    @Override
    public String getPermission() {
        return "atla.command.reload";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
