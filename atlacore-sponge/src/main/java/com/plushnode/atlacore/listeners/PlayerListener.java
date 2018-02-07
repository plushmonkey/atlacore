package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.board.BendingBoard;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.air.AirSpout;
import com.plushnode.atlacore.game.ability.fire.Combustion;
import com.plushnode.atlacore.game.ability.fire.FireBurst;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.ability.fire.HeatControl;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlast;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlaze;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.events.PlayerToggleSneakEvent;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacore.util.WorldUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.*;

public class PlayerListener {
    private AtlaPlugin plugin;
    // TODO: Move into appropriate place
    private Map<String, BendingBoard> boards = new HashMap<>();
    private Task boardTask = null;

    public PlayerListener(AtlaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean activateAbility(User user, ActivationMethod method) {
        AbilityDescription abilityDescription = user.getSelectedAbility();

        if (abilityDescription == null) return false;
        if (!abilityDescription.isActivatedBy(method)) return false;
        if (user.isOnCooldown(abilityDescription)) return false;
        if (!abilityDescription.isEnabled()) return false;

        String permission = "atla.ability." + abilityDescription.getName();

        if (!user.hasPermission(permission)) return false;

        Ability ability = abilityDescription.createAbility();

        if (ability.activate(user, method)) {
            plugin.getGame().addAbility(user, ability);
        } else {
            return false;
        }

        return true;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();

        // Create the player on the next tick so createPlayer can find the Sponge player.
        plugin.createTask(() -> {
            Game.getPlayerService().createPlayer(player.getUniqueId(), player.getName(), (p) -> {
                if (p == null) {
                    // This can happen if the player logs off before the player is created.
                    return;
                }

                boards.put(p.getName(), new BendingBoard(p));

                if (boardTask == null) {
                    boardTask = plugin.createTaskTimer(() -> {
                        for (BendingBoard board : boards.values()) {
                            board.update();
                        }
                    }, 5, 5);
                }

                Game.getAbilityInstanceManager().createPassives(p);
            });
        }, 1);
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        Player spongePlayer = event.getTargetEntity();

        com.plushnode.atlacore.platform.Player player
                = Game.getPlayerService().getPlayerByName(spongePlayer.getName());

        Game.getPlayerService().savePlayer(player, (p) -> {
            Game.info(p.getName() + " saved to database.");
        });

        Game.getPlayerService().invalidatePlayer(player);

        boards.remove(player.getName());

        Game.getAbilityInstanceManager().clearPassives(player);
    }

    @Listener
    public void onPlayerMove(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        User user = Game.getPlayerService().getPlayerByName(((Player) event.getTargetEntity()).getName());
        if (user == null) return;

        Vector3D velocity = SpongeTypeUtil.adapt(event.getToTransform().getPosition().sub(event.getFromTransform().getPosition()));

        AirSpout.handleMovement(user, velocity);
    }

    @Listener
    public void onInteractEntity(InteractEntityEvent event) {
        Optional<Player> result = event.getCause().last(Player.class);
        if (!result.isPresent()) {
            return;
        }

        Player player = result.get();
        User user = Game.getPlayerService().getPlayerByName(player.getName());
        Game.getSequenceService().registerAction(user, Action.InteractEntity);
    }

    @Listener
    public void onInteractBlock(InteractBlockEvent.Secondary.MainHand event) {
        Optional<Player> result = event.getCause().last(Player.class);
        if (!result.isPresent()) {
            return;
        }

        Player player = result.get();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (event.getTargetBlock().getState().getType() == BlockTypes.AIR) {
            Game.getSequenceService().registerAction(user, Action.Interact);
        } else {
            Game.getSequenceService().registerAction(user, Action.InteractBlock);
        }
    }

    @Listener
    public void onFallDamage(DamageEntityEvent event, @Root DamageSource source) {
        if (source.getType() != DamageTypes.FALL) return;

        Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        activateAbility(user, ActivationMethod.Fall);

        if (user.hasElement(Elements.AIR)) {
            event.setCancelled(true);
        }

        if (Flight.hasFlight(user)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onFireTickDamage(DamageEntityEvent event, @Root DamageSource source) {
        if (source.getType() != DamageTypes.FIRE) return;

        Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (!HeatControl.canBurn(user)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        Game.getSequenceService().registerAction(user, event.isSneaking() ? Action.Sneak : Action.SneakRelease);

        if (event.isSneaking()) {
            activateAbility(user, ActivationMethod.Sneak);

            Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
        }
    }

    @Listener
    public void onPlayerPunch(AnimateHandEvent event) {
        Entity entity = event.getTargetEntity();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class)) {
            if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("AirScooter")) {
                return;
            }
        }

        if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("FireJet")) {
            if (Game.getAbilityInstanceManager().destroyInstanceType(user, FireJet.class)) {
                return;
            }

            if (Game.getAbilityInstanceManager().destroyInstanceType(user, JetBlast.class)) {
                return;
            }

            if (Game.getAbilityInstanceManager().destroyInstanceType(user, JetBlaze.class)) {
                return;
            }
        }

        Combustion.combust(user);
        FireBurst.activateConalBurst(user);

        if (WorldUtil.getTargetEntity(user, 4) != null) {
            Game.getSequenceService().registerAction(user, Action.PunchEntity);
        } else {
            Game.getSequenceService().registerAction(user, Action.Punch);
        }

        activateAbility(user, ActivationMethod.Punch);
    }
}
