package com.plushnode.atlacore.command;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.util.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayCommand implements CoreCommand {
    private String[] aliases = { "display", "d" };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/bending display [element]");
            return true;
        }

        Element element = getClosestElement(args[1]);

        if (element == null) {
            sender.sendMessage(ChatColor.RED + "There is no element named " + args[1] + ".");
            return true;
        }


        List<String> outputs = new ArrayList<>();
        List<String> normals = getNormalAbilities(element);

        if (!normals.isEmpty()) {
            outputs.add(ChatColor.GOLD + "Abilities:");
            outputs.addAll(normals);
        }

        List<String> sequences = getActivatedAbilities(element, ActivationMethod.Sequence);

        if (!sequences.isEmpty()) {
            outputs.add(ChatColor.GOLD + "Sequences: ");
            outputs.addAll(sequences);
        }

        List<String> passives = getActivatedAbilities(element, ActivationMethod.Passive);

        if (!passives.isEmpty()) {
            outputs.add(ChatColor.GOLD + "Passives: ");
            outputs.addAll(passives);
        }

        if (outputs.isEmpty()) {
            sender.sendMessage(element.toString() + ChatColor.GOLD + " has no abilities.");
            return true;
        }

        for (String str : outputs) {
            sender.sendMessage(str);
        }

        return true;
    }

    private List<String> getNormalAbilities(Element element) {
        List<String> outputs = new ArrayList<>();

        for (AbilityDescription desc : Game.getAbilityRegistry().getAbilities()) {
            if (desc.isActivatedBy(ActivationMethod.Sequence)) continue;
            if (desc.isActivatedBy(ActivationMethod.Passive)) continue;

            if (desc.getElement() == element && !desc.isHidden()) {
                outputs.add(desc.toString());
            }
        }

        Collections.sort(outputs);

        return outputs;
    }

    private List<String> getActivatedAbilities(Element element, ActivationMethod method) {
        List<String> outputs = new ArrayList<>();

        for (AbilityDescription desc : Game.getAbilityRegistry().getAbilities()) {
            if (!desc.isActivatedBy(method)) continue;

            if (desc.getElement() == element) {
                outputs.add(desc.toString());
            }
        }

        Collections.sort(outputs);

        return outputs;
    }

    private Element getClosestElement(String name) {
        Element closest = null;

        for (Element element : Game.getElementRegistry().getElements()) {
            if (element.getName().toLowerCase().startsWith(name.toLowerCase())) {
                closest = element;
            }
        }

        return closest;
    }

    @Override
    public String getDescription() {
        return "Lists the available abilities.";
    }

    @Override
    public String getPermission() {
        return "bending.command.display";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
