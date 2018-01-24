package com.plushnode.atlacore.command;

public interface CoreCommand {
    boolean execute(CommandSender sender, String[] args);

    // A short description used when the main command is sent without args.
    String getDescription();
    String getPermission();
    String[] getAliases();
}
