package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;

public class ReloadCommand implements CoreCommand {
    private String[] aliases = { "reload" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Game.reload();

        sender.sendMessage("Bending plugin config reloaded.");

        return true;
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin config.";
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
