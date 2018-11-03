package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.util.ChatColor;

public class PresetCommand implements CoreCommand {
    private String[] aliases = { "preset", "p" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/bending preset [bind/create/delete/list]");
            return true;
        }

        String action = args[1];

        Player player = (Player)sender;

        switch (action.charAt(0)) {
            case 'c':
            case 'C':
            {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.GREEN + "/bending preset create [presetName]");
                    return true;
                }

                String presetName = args[2];

                Game.getPresetService().createPreset(player, presetName, (preset) -> {
                    sender.sendMessage(ChatColor.GREEN + "Successfully created preset with the name '" + preset.getName() + "'.");
                });
            }
            break;
            case 'l':
            case 'L':
            {
                Game.getPresetService().getPresetNames(player, (presetNames) -> {
                    if (!presetNames.isEmpty()) {
                        String presets = String.join(", ", presetNames);

                        sender.sendMessage(ChatColor.GOLD + presets);
                    } else {
                        sender.sendMessage(ChatColor.RED + "No presets found.");
                    }
                });
            }
            break;
            case 'd':
            case 'D':
            {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.GREEN + "/bending preset delete [presetName]");
                    return true;
                }

                String presetName = args[2];
                Game.getPresetService().deletePreset(player, presetName, (success) -> {
                    if (success) {
                        sender.sendMessage(ChatColor.GREEN + "Successfully deleted preset '" + presetName + "'.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to delete preset '" + presetName + "'.");
                    }
                });
            }
            break;
            case 'b':
            case 'B':
            {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.GREEN + "/bending preset bind [presetName]");
                    return true;
                }

                String presetName = args[2];
                Game.getPresetService().loadPreset(player, presetName, (preset) -> {
                    if (preset == null) {
                        sender.sendMessage(ChatColor.RED + "No preset found with the name '" + presetName + "'.");
                        return;
                    }

                    int count = 0;
                    int total = 0;
                    for (int i = 0; i < preset.getAbilities().size(); ++i) {
                        AbilityDescription desc = preset.getAbilities().get(i);

                        if (desc != null) {
                            ++total;
                            if (player.canBind(desc)) {
                                player.setSlotAbility(i + 1, desc);
                                ++count;
                            } else {
                                player.setSlotAbility(i + 1, null);
                            }
                        } else {
                            player.setSlotAbility(i + 1, null);
                        }
                    }

                    if (count > 0 || total == 0) {
                        sender.sendMessage(ChatColor.GREEN + "Successfully bound " + count + "/" + total + " abilities.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "No abilities could be bound from the preset.");
                    }

                    Game.getPlayerService().saveSlots(player);
                });
            }
            break;
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Manage presets.";
    }

    @Override
    public String getPermission() {
        return "atla.command.preset";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
