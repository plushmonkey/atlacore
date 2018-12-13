package com.plushnode.atlacore.game;

import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.air.AirBurst;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.game.ability.air.AirSpout;
import com.plushnode.atlacore.game.ability.air.passives.GracefulDescent;
import com.plushnode.atlacore.game.ability.earth.passives.DensityShift;
import com.plushnode.atlacore.game.ability.fire.Combustion;
import com.plushnode.atlacore.game.ability.fire.FireBurst;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.ability.fire.HeatControl;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlast;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlaze;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.WorldUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class ActivationController {
    public boolean activateAbility(User user, ActivationMethod method) {
        AbilityDescription abilityDescription = user.getSelectedAbility();

        if (abilityDescription == null) return false;
        if (!abilityDescription.isActivatedBy(method)) return false;
        if (!user.canBend(abilityDescription)) return false;

        Ability ability = abilityDescription.createAbility();

        if (ability.activate(user, method)) {
            Game.addAbility(user, ability);
        } else {
            return false;
        }

        return true;
    }

    public void onPlayerLogout(Player player) {
        player.popSlotContainer();
        Game.getAttributeSystem().clearModifiers(player);

        Game.getPlayerService().savePlayer(player, (p) -> {
            Game.info(p.getName() + " saved to database.");
        });

        Game.getPlayerService().invalidatePlayer(player);
        Game.getAbilityInstanceManager().clearPassives(player);
    }

    public void onUserSwing(User user) {
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
        FireBurst.activateCone(user);
        AirBurst.activateCone(user);

        if (WorldUtil.getTargetEntity(user, 4) != null) {
            Game.getSequenceService().registerAction(user, Action.PunchEntity);
        } else {
            Game.getSequenceService().registerAction(user, Action.Punch);
        }

        activateAbility(user, ActivationMethod.Punch);
    }

    public void onUserSneak(User user, boolean sneaking) {
        Game.getSequenceService().registerAction(user, sneaking ? Action.Sneak : Action.SneakRelease);

        if (!sneaking) return;

        activateAbility(user, ActivationMethod.Sneak);

        Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
    }

    public void onUserMove(User user, Vector3D velocity) {
        AirSpout.handleMovement(user, velocity);
    }

    public boolean onFallDamage(User user) {
        activateAbility(user, ActivationMethod.Fall);

        if (user.hasElement(Elements.AIR) && GracefulDescent.isGraceful(user)) {
            return false;
        }

        if (user.hasElement(Elements.EARTH) && DensityShift.isSoftened(user)) {
            Block block = user.getLocation().getBlock().getRelative(BlockFace.DOWN);
            Location location = block.getLocation().add(0.5, 0.5, 0.5);
            DensityShift.softenArea(user, location);
            return false;
        }

        return !Flight.hasFlight(user);
    }

    public void onUserInteract(User user, boolean rightClickAir) {
        if (rightClickAir) {
            Game.getSequenceService().registerAction(user, Action.Interact);
        } else {
            Game.getSequenceService().registerAction(user, Action.InteractBlock);
            activateAbility(user, ActivationMethod.Use);
        }
    }

    public void onUserInteractEntity(User user) {
        Game.getSequenceService().registerAction(user, Action.InteractEntity);
    }

    public boolean onFireTickDamage(User user) {
        return HeatControl.canBurn(user);
    }
}
