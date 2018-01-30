package com.plushnode.atlacore.board;


import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.platform.SpongeBendingPlayer;
import com.plushnode.atlacore.util.ChatColor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import java.util.*;

public class BendingBoard {
    private Player spongePlayer;
    private SpongeBendingPlayer bendingPlayer;
    private Scoreboard scoreboard;
    private Set<Text> updatedScores = new HashSet<>();
    private Objective objective;

    public BendingBoard(com.plushnode.atlacore.platform.Player player) {
        this.bendingPlayer = (SpongeBendingPlayer)player;
        this.spongePlayer = bendingPlayer.getSpongePlayer();
        this.scoreboard = Scoreboard.builder().build();
        this.scoreboard.registerTeam(Team.builder().name(player.getName() + "-team").build());
        this.objective = Objective.builder().name("Slots").criterion(Criteria.DUMMY).build();

        scoreboard.addObjective(this.objective);

        this.objective.setDisplayName(Text.of(ChatColor.BOLD + "Slots"));
        scoreboard.updateDisplaySlot(this.objective, DisplaySlots.SIDEBAR);

        bendingPlayer.getSpongePlayer().setScoreboard(scoreboard);
    }

    public void update() {
        bendingPlayer = (SpongeBendingPlayer)Game.getPlayerService().getPlayerByName(spongePlayer.getName());

        if (spongePlayer.getScoreboard() != scoreboard) {
            spongePlayer.setScoreboard(scoreboard);
        }

        updatedScores.clear();

        updateSlots();
        updateSequences();

        // Clear out any scores that aren't needed.
        for (Score score : scoreboard.getScores()) {
            Text name = score.getName();

            if (updatedScores.contains(name)) continue;

            scoreboard.removeScores(score.getName());
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

            updatedScores.add(Text.of(sb.toString()));

            Score score = objective.getOrCreateScore(Text.of(sb.toString()));
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
            Score score = objective.getOrCreateScore(Text.of(update));

            if (score.getScore() != -slotIndex) {
                score.setScore(-slotIndex);
            }

            updatedScores.add(Text.of(update));
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
