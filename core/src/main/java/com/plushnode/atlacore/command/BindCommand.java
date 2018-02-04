package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.util.ChatColor;

public class BindCommand implements CoreCommand {
    private String[] aliases = { "bind", "b" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof User)) {
            sender.sendMessage(ChatColor.RED + "Only users can execute this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/bending bind [abilityName] <slot#>");
            return true;
        }

        String abilityName = args[1];
        AbilityDescription abilityDesc = Game.getAbilityRegistry().getAbilityByName(abilityName);
        if (abilityDesc == null) {
            sender.sendMessage(ChatColor.RED + "Could not find ability named " + abilityName + ".");
            return true;
        }

        if (abilityDesc.isActivatedBy(ActivationMethod.Sequence)) {
            sender.sendMessage(ChatColor.RED + "Cannot bind sequence abilities.");
            return true;
        }

        if (abilityDesc.isActivatedBy(ActivationMethod.Passive)) {
            sender.sendMessage(ChatColor.RED + "Cannot bind passive abilities.");
            return true;
        }

        if (abilityDesc.isHidden()) {
            sender.sendMessage(ChatColor.RED + "Cannot bind hidden abilities.");
            return true;
        }

        Player player = (Player) sender;

        int slot;
        if (args.length >= 3) {
            try {
                slot = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Failed to parse slot index.");
                return true;
            }
        } else {
            slot = player.getHeldItemSlot() + 1;
        }

        if (slot < 1 || slot > 9) {
            sender.sendMessage(ChatColor.RED + "Invalid slot number. Slot must be between 1 and 9.");
            return true;
        }

        Element element = abilityDesc.getElement();
        if (!player.hasElement(element)) {
            sender.sendMessage(ChatColor.RED + abilityDesc.getName() + " requires element " + element.toString() + ChatColor.RED + ".");
            return true;
        }

        player.setSlotAbility(slot, abilityDesc);
        Game.getPlayerService().saveSlot(player, slot);

        sender.sendMessage(ChatColor.GOLD + abilityDesc.toString() + ChatColor.GOLD + " was bound to slot " + slot + ".");
        return true;
    }

    @Override
    public String getDescription() {
        return "Binds an ability to a slot.";
    }

    @Override
    public String getPermission() {
        return "bending.command.bind";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
