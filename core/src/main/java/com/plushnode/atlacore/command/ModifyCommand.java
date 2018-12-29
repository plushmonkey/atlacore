package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.attribute.AttributeModifier;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.game.attribute.ModifierOperation;
import com.plushnode.atlacore.game.attribute.ModifyPolicy;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ModifyCommand implements CoreCommand {
    private String[] aliases = { "modify", "m" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.GREEN + "/bending modify [add/clear] [player] [policy] [type] [operation] [amount]");
            return true;
        }

        Player player = Game.getPlayerService().getPlayerByName(args[2]);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player could not be found.");
            return true;
        }

        String subCommand = args[1];

        if (subCommand.toLowerCase().charAt(0) == 'c') {
            Game.getAttributeSystem().clearModifiers(player);
            Game.getAttributeSystem().recalculate(player);

            sender.sendMessage(ChatColor.GREEN + "Cleared attribute modifiers for " + player.getName() + ".");
            return true;
        }

        if (args.length < 7) {
            sender.sendMessage(ChatColor.GREEN + "/bending modify [add/clear] [player] [context] [type] [operation] [amount]");
            return true;
        }

        ModifyPolicy policy = parsePolicy(args[3]);
        if (policy == null) {
            sender.sendMessage(ChatColor.GOLD + "Invalid policy. Policy must be an element or ability name.");
            return true;
        }

        String type = parseAttributeType(args[4]);

        if (type == null) {
            sender.sendMessage(ChatColor.GOLD + "Invalid attribute type.");
            return true;
        }

        ModifierOperation operation = parseOperation(args[5]);
        double amount = 0;

        try {
            amount = Double.parseDouble(args[6]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Could not parse amount.");
            return true;
        }

        AttributeModifier modifier = new AttributeModifier(type, operation, amount);
        Game.getAttributeSystem().addModifier(player, modifier, policy);
        Game.getAttributeSystem().recalculate(player);

        sender.sendMessage(ChatColor.GREEN + "Successfully added modifier to " + player.getName() + ".");
        return true;
    }

    private ModifierOperation parseOperation(String str) {
        str = str.toLowerCase();

        if (str.charAt(0) == 'm')
            return ModifierOperation.MULTIPLICATIVE;
        if (str.charAt(0) == 's')
            return ModifierOperation.SUMMED_MULTIPLICATIVE;
        return ModifierOperation.ADDITIVE;
    }

    private ModifyPolicy parsePolicy(String str) {
        Element element = Game.getElementRegistry().getElementByName(str);

        if (element != null) {
            return ability -> ability.getDescription().getElement() == element;
        }

        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName(str);

        if (desc != null) {
            return ability -> ability.getDescription() == desc;
        }

        return null;
    }

    private String parseAttributeType(String str) {
        List<String> validAttributes = Arrays.asList(Attributes.TYPES);

        return validAttributes.stream().filter(attr -> attr.equalsIgnoreCase(str)).findAny().orElse(null);
    }

    @Override
    public String getDescription() {
        return "Manages attribute modifiers for a player.";
    }

    @Override
    public String getPermission() {
        return "atla.command.modify";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
