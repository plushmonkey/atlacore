package com.plushnode.atlacore.board;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BendingBoard {
    private Player bukkitPlayer;
    private BukkitBendingPlayer bendingPlayer;
    private org.bukkit.scoreboard.Scoreboard scoreboard;
    private Team team;
    private Objective objective;
    private Set<String> updatedScores = new HashSet<>();

    public BendingBoard(com.plushnode.atlacore.platform.Player player) {
        this.bendingPlayer = (BukkitBendingPlayer)player;
        this.bukkitPlayer = bendingPlayer.getBukkitPlayer();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.team = scoreboard.registerNewTeam(player.getName() + "-team");
        this.objective = scoreboard.registerNewObjective("Slots", "dummy");
        this.objective.setDisplayName(ChatColor.BOLD + "Slots");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        bukkitPlayer.setScoreboard(scoreboard);
    }

    public void update() {
        bendingPlayer = (BukkitBendingPlayer)Game.getPlayerService().getPlayerByName(bukkitPlayer.getName());

        if (bukkitPlayer.getScoreboard() != scoreboard) {
            bukkitPlayer.setScoreboard(scoreboard);
        }

        updatedScores.clear();

        updateSlots();
        updateSequences();

        // Clear out any scores that aren't needed.
        for (String entry : scoreboard.getEntries()) {
            if (updatedScores.contains(entry)) continue;
            scoreboard.resetScores(entry);
        }
    }

    private void updateSlots() {
        int currentSlot = bendingPlayer.getHeldItemSlot() + 1;

        for (int slotIndex = 1; slotIndex <= 9; ++slotIndex) {
            StringBuilder sb = new StringBuilder();

            AbilityDescription desc = bendingPlayer.getSlotAbility(slotIndex);

            sb.append(getUniquePrefix(slotIndex));

            if (slotIndex == currentSlot) {
                sb.append(">");
            }

            if (desc == null) {
                sb.append(ChatColor.DARK_GRAY).append(ChatColor.ITALIC);
                sb.append("-- Slot ").append(slotIndex).append(" --");
            } else {
                sb.append(desc.getElement().getColor());

                if (bendingPlayer.isOnCooldown(desc)) {
                    sb.append(ChatColor.STRIKETHROUGH);
                }

                sb.append(desc.getName());
            }

            updatedScores.add(sb.toString());

            Score score = objective.getScore(sb.toString());
            // Only set the new score if it changes.
            if (score.getScore() != -slotIndex) {
                score.setScore(-slotIndex);
            }
        }
    }

    private void updateSequences() {
        int slotIndex = 10;

        List<String> updates = new ArrayList<>();

        for (AbilityDescription description : Game.getAbilityRegistry().getAbilities()) {
            if (description.isActivatedBy(ActivationMethod.Sequence)) {
                if (bendingPlayer.isOnCooldown(description)) {
                    String name = getUniquePrefix(slotIndex);

                    name += description.getElement().getColor();
                    name += ChatColor.STRIKETHROUGH;
                    name += description.getName();

                    if (updates.isEmpty()) {
                        updates.add(ChatColor.BOLD + "Sequences");
                    }

                    updates.add(name);
                }
            }
        }

        for (String update : updates) {
            Score score = objective.getScore(update);

            if (score.getScore() != -slotIndex) {
                score.setScore(-slotIndex);
            }

            updatedScores.add(update);
            ++slotIndex;
        }
    }

    // Make sure each entry has its own unique string.
    // This makes it so it doesn't remove duplicate binds.
    private String getUniquePrefix(int index) {
        if (index < 22) {
            return ChatColor.values()[index].toString() + ChatColor.RESET;
        }

        return ChatColor.RESET + getUniquePrefix(index - 22);
    }
}
