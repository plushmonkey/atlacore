package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.sequence.AbilityAction;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.ability.sequence.Sequence;
import com.plushnode.atlacore.util.ChatColor;

import java.util.Iterator;
import java.util.List;

public class HelpCommand implements CoreCommand {
    private String[] aliases = { "help", "h" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/bending help [abilityName]");
            return true;
        }

        String abilityName = args[1];
        AbilityDescription abilityDesc = Game.getAbilityRegistry().getAbilityByName(abilityName);
        if (abilityDesc == null) {
            sender.sendMessage(ChatColor.RED + "Could not find ability named " + abilityName + ".");
            return true;
        }

        String description = abilityDesc.getDescription();
        String instructions = abilityDesc.getInstructions();

        if (description.isEmpty() && instructions.isEmpty()) {
            sender.sendMessage(abilityDesc.toString() + ChatColor.GOLD + ": No description or instructions set.");
        } else {
            if (!description.isEmpty()) {
                sender.sendMessage(abilityDesc.toString() + " " + ChatColor.GOLD + "description: \n" + description);
            }

            if (!instructions.isEmpty()) {
                sender.sendMessage(abilityDesc.toString() + " " + ChatColor.GOLD + "instructions: \n" + instructions);
            }
        }

        if (abilityDesc.isActivatedBy(ActivationMethod.Sequence)) {
            Sequence sequence = Game.getSequenceService().getSequence(abilityDesc);
            if (sequence != null) {
                String sequenceInstructions = getSequenceInstructions(sequence);
                sender.sendMessage(abilityDesc.toString() + " " + ChatColor.GOLD + "sequence:\n" + sequenceInstructions);
            }
        }

        return true;
    }

    private String getSequenceInstructions(Sequence sequence) {
        StringBuilder sb = new StringBuilder();

        List<AbilityAction> actions = sequence.getActions();

        for (int i = 0; i < actions.size(); ++i) {
            AbilityAction abilityAction = actions.get(i);

            if (i != 0) {
                sb.append(" > ");
            }

            AbilityDescription desc = abilityAction.getAbilityDescription();
            Action action = abilityAction.getAction();
            String actionString = action.toString();

            if (action == Action.Sneak) {
                actionString = "Hold Sneak";

                // Check if the next instruction is to release this sneak.
                if (i + 1 < actions.size()) {
                    AbilityAction next = actions.get(i + 1);
                    if (next.getAbilityDescription() == desc && next.getAction() == Action.SneakRelease) {
                        actionString = "Tap Sneak";
                        ++i;
                    }
                }
            }

            sb.append(desc.toString()).append(ChatColor.GOLD);
            sb.append(" (").append(actionString).append(")");
        }

        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "Explains how to use an ability.";
    }

    @Override
    public String getPermission() {
        return "atla.command.help";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
