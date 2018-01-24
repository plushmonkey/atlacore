package com.plushnode.atlacore.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommandRegistry {
    private Map<String, CoreCommand> commands = new HashMap<>();

    public void registerCommand(CoreCommand command) {
        String[] aliases = command.getAliases();

        for (String alias : aliases) {
            commands.put(alias.toLowerCase(), command);
        }
    }

    public void unregisterCommand(String commandName) {
        CoreCommand command = commands.get(commandName.toLowerCase());

        if (command == null) return;

        Iterator<Map.Entry<String, CoreCommand>> iterator = commands.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CoreCommand> entry = iterator.next();
            if (entry.getValue() == command) {
                iterator.remove();
            }
        }
    }

    public Map<String, CoreCommand> getCommands() {
        return commands;
    }
}
