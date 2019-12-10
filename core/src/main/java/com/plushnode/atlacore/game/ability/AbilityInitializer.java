package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.collision.CollisionService;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.air.*;
import com.plushnode.atlacore.game.ability.air.passives.AirAgility;
import com.plushnode.atlacore.game.ability.air.passives.GracefulDescent;
import com.plushnode.atlacore.game.ability.air.sequences.AirStream;
import com.plushnode.atlacore.game.ability.air.sequences.AirSweep;
import com.plushnode.atlacore.game.ability.air.sequences.Twister;
import com.plushnode.atlacore.game.ability.earth.*;
import com.plushnode.atlacore.game.ability.earth.passives.DensityShift;
import com.plushnode.atlacore.game.ability.fire.*;
import com.plushnode.atlacore.game.ability.fire.sequences.*;
import com.plushnode.atlacore.game.ability.sequence.AbilityAction;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.ability.sequence.Sequence;
import com.plushnode.atlacore.game.ability.sequence.SequenceService;
import com.plushnode.atlacore.game.ability.water.*;
import com.plushnode.atlacore.game.ability.water.arms.*;
import com.plushnode.atlacore.game.ability.water.surge.Surge;
import com.plushnode.atlacore.game.ability.water.surge.SurgeWall;
import com.plushnode.atlacore.game.ability.water.surge.SurgeWave;
import com.plushnode.atlacore.game.ability.water.torrent.Torrent;
import com.plushnode.atlacore.game.ability.water.torrent.TorrentWave;
import com.plushnode.atlacore.game.ability.water.util.BottleReturn;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.game.element.Elements;

// TODO: This needs to be cleaned up.
public final class AbilityInitializer {
    private AbilityInitializer() {

    }

    public static void loadAbilities() {
        AbilityRegistry abilityRegistry = Game.getAbilityRegistry();
        SequenceService sequenceService = Game.getSequenceService();
        CollisionService collisionService = Game.getCollisionService();

        AbilityDescription blaze = registerAbility("Blaze", Blaze.class, Elements.FIRE, ActivationMethod.Punch, ActivationMethod.Sneak);
        AbilityDescription fireBlast = registerAbility("FireBlast", FireBlast.class, Elements.FIRE, ActivationMethod.Punch, ActivationMethod.Sneak);
        AbilityDescription fireBlastCharged = registerAbility("FireBlastCharged", FireBlastCharged.class, Elements.FIRE, ActivationMethod.Sneak).setHidden(true);
        AbilityDescription fireJet = registerAbility("FireJet", FireJet.class, Elements.FIRE, ActivationMethod.Punch).setHarmless(true);
        AbilityDescription fireShield = registerAbility("FireShield", FireShield.class, Elements.FIRE, ActivationMethod.Punch, ActivationMethod.Sneak);
        registerAbility("FireWall", FireWall.class, Elements.FIRE, ActivationMethod.Punch);
        registerAbility("HeatControl", HeatControl.class, Elements.FIRE, ActivationMethod.Punch);
        registerAbility("Lightning", Lightning.class, Elements.FIRE, ActivationMethod.Sneak);
        registerAbility("Combustion", Combustion.class, Elements.FIRE, ActivationMethod.Sneak);
        registerAbility("FireBurst", FireBurst.class, Elements.FIRE, ActivationMethod.Sneak);
        AbilityDescription fireKick = registerAbility("FireKick", FireKick.class, Elements.FIRE, ActivationMethod.Sequence);
        AbilityDescription jetBlast = registerAbility("JetBlast", JetBlast.class, Elements.FIRE, ActivationMethod.Sequence).setHarmless(true);
        AbilityDescription jetBlaze = registerAbility("JetBlaze", JetBlaze.class, Elements.FIRE, ActivationMethod.Sequence);
        AbilityDescription fireSpin = registerAbility("FireSpin", FireSpin.class, Elements.FIRE, ActivationMethod.Sequence);
        AbilityDescription fireWheel = registerAbility("FireWheel", FireWheel.class, Elements.FIRE, ActivationMethod.Sequence);

        registerAbility("AirScooter", AirScooter.class, Elements.AIR, ActivationMethod.Punch).setHarmless(true);
        AbilityDescription airSwipe = registerAbility("AirSwipe", AirSwipe.class, Elements.AIR, ActivationMethod.Punch, ActivationMethod.Sneak);
        AbilityDescription airBlast = registerAbility("AirBlast", AirBlast.class, Elements.AIR, ActivationMethod.Punch, ActivationMethod.Sneak);
        AbilityDescription airShield = registerAbility("AirShield", AirShield.class, Elements.AIR, ActivationMethod.Sneak);
        registerAbility("AirSpout", AirSpout.class, Elements.AIR, ActivationMethod.Punch).setHarmless(true);
        AbilityDescription airBurst = registerAbility("AirBurst", AirBurst.class, Elements.AIR, ActivationMethod.Sneak, ActivationMethod.Fall);
        AbilityDescription tornado = registerAbility("Tornado", Tornado.class, Elements.AIR, ActivationMethod.Sneak);
        AbilityDescription airSuction = registerAbility("AirSuction", AirSuction.class, Elements.AIR, ActivationMethod.Punch, ActivationMethod.Sneak);
        registerAbility("Suffocate", Suffocate.class, Elements.AIR, ActivationMethod.Sneak);
        AbilityDescription airSweep = registerAbility("AirSweep", AirSweep.class, Elements.AIR, ActivationMethod.Sequence);
        AbilityDescription twister = registerAbility("Twister", Twister.class, Elements.AIR, ActivationMethod.Sequence);
        AbilityDescription airStream = registerAbility("AirStream", AirStream.class, Elements.AIR, ActivationMethod.Sequence);
        registerAbility("AirAgility", AirAgility.class, Elements.AIR, ActivationMethod.Passive).setHarmless(true).setHidden(true);
        registerAbility("GracefulDescent", GracefulDescent.class, Elements.AIR, ActivationMethod.Passive).setHarmless(true).setHidden(true);

        registerAbility("Shockwave", Shockwave.class, Elements.EARTH, ActivationMethod.Punch, ActivationMethod.Sneak, ActivationMethod.Fall);
        registerAbility("EarthBlast", EarthBlast.class, Elements.EARTH, ActivationMethod.Punch, ActivationMethod.Sneak)
                .setCanBypassCooldown(true);
        registerAbility("Catapult", Catapult.class, Elements.EARTH, ActivationMethod.Punch).setHarmless(true);
        registerAbility("Collapse", Collapse.class, Elements.EARTH, ActivationMethod.Punch, ActivationMethod.Sneak);
        registerAbility("RaiseEarth", RaiseEarth.class, Elements.EARTH, ActivationMethod.Punch, ActivationMethod.Sneak);
        registerAbility("EarthSmash", EarthSmash.class, Elements.EARTH, ActivationMethod.Punch, ActivationMethod.Sneak, ActivationMethod.Use);
        registerAbility("DensityShift", DensityShift.class, Elements.EARTH, ActivationMethod.Passive).setHarmless(true).setHidden(true);
        registerAbility("EarthArmor", EarthArmor.class, Elements.EARTH, ActivationMethod.Sneak);
        registerAbility("EarthTunnel", EarthTunnel.class, Elements.EARTH, ActivationMethod.Sneak);
        registerAbility("EarthGrab", EarthGrab.class, Elements.EARTH, ActivationMethod.Punch);

        GenericAbilityDescription surgeDesc = registerAbility("Surge", Surge.class, Elements.WATER, ActivationMethod.Punch, ActivationMethod.Sneak);
        surgeDesc.setCanBypassCooldown(true);
        surgeDesc.setSourcesPlants(true);
        registerAbility("SurgeWall", SurgeWall.class, Elements.WATER, ActivationMethod.Punch).setHidden(true);
        registerAbility("SurgeWave", SurgeWave.class, Elements.WATER, ActivationMethod.Punch).setHidden(true);
        registerAbility("BottleReturn", BottleReturn.class, Elements.WATER, ActivationMethod.Punch).setHidden(true);
        GenericAbilityDescription torrentDesc = registerAbility("Torrent", Torrent.class, Elements.WATER, ActivationMethod.Punch, ActivationMethod.Sneak);
        torrentDesc.setCanBypassCooldown(true);
        torrentDesc.setSourcesPlants(true);
        registerAbility("TorrentWave", TorrentWave.class, Elements.WATER, ActivationMethod.Punch).setHidden(true);

        registerAbility("WaterArms", WaterArms.class, Elements.WATER, ActivationMethod.Sneak);
        registerAbility("WaterBubble", WaterBubble.class, Elements.WATER, ActivationMethod.Punch, ActivationMethod.Sneak);
        registerAbility("WaterManipulation", WaterManipulation.class, Elements.WATER, ActivationMethod.Punch, ActivationMethod.Sneak)
                .setCanBypassCooldown(true);
        registerAbility("WaterSpout", WaterSpout.class, Elements.WATER, ActivationMethod.Punch)
                .setSourcesPlants(true);
        registerAbility("PhaseChange", PhaseChange.class, Elements.WATER, ActivationMethod.Punch, ActivationMethod.Sneak);

        ConfigurableAbilityDescription<WaterSpoutWave> waterSpoutWave = new ConfigurableAbilityDescription<>("WaterSpoutWave", Elements.WATER, 0, WaterSpoutWave.class, ActivationMethod.Punch);

        waterSpoutWave.setConfigNode("abilities", "water", "waterspout", "wave");
        waterSpoutWave.setHidden(true);
        abilityRegistry.registerAbility(waterSpoutWave);

        MultiAbilityDescription pullDesc = new MultiAbilityDescription<>("WaterArmsPull", Elements.WATER, 0, WaterArmsPull.class, ActivationMethod.Punch);
        pullDesc.setConfigNode("waterarms", "pull");
        pullDesc.setHidden(true);
        pullDesc.setDisplayName("Pull");
        abilityRegistry.registerAbility(pullDesc);

        MultiAbilityDescription punchDesc = new MultiAbilityDescription<>("WaterArmsPunch", Elements.WATER, 0, WaterArmsPunch.class, ActivationMethod.Punch);
        punchDesc.setConfigNode("waterarms", "punch");
        punchDesc.setHidden(true);
        punchDesc.setDisplayName("Punch");
        abilityRegistry.registerAbility(punchDesc);

        MultiAbilityDescription grappleDesc = new MultiAbilityDescription<>("WaterArmsGrapple", Elements.WATER, 0, WaterArmsGrapple.class, ActivationMethod.Punch);
        grappleDesc.setConfigNode("waterarms", "grapple");
        grappleDesc.setHidden(true);
        grappleDesc.setDisplayName("Grapple");
        abilityRegistry.registerAbility(grappleDesc);

        MultiAbilityDescription grabDesc = new MultiAbilityDescription<>("WaterArmsGrab", Elements.WATER, 0, WaterArmsGrab.class, ActivationMethod.Punch);
        grabDesc.setConfigNode("waterarms", "grab");
        grabDesc.setHidden(true);
        grabDesc.setDisplayName("Grab");
        abilityRegistry.registerAbility(grabDesc);

        MultiAbilityDescription freezeDesc = new MultiAbilityDescription<>("WaterArmsFreeze", Elements.WATER, 0, WaterArmsFreeze.class, ActivationMethod.Punch);
        freezeDesc.setConfigNode("waterarms", "freeze");
        freezeDesc.setHidden(true);
        freezeDesc.setDisplayName("Freeze");
        abilityRegistry.registerAbility(freezeDesc);

        MultiAbilityDescription spearDesc = new MultiAbilityDescription<>("WaterArmsSpear", Elements.WATER, 0, WaterArmsSpear.class, ActivationMethod.Punch);
        spearDesc.setConfigNode("waterarms", "spear");
        spearDesc.setHidden(true);
        spearDesc.setDisplayName("Spear");
        abilityRegistry.registerAbility(spearDesc);

        sequenceService.registerSequence(fireKick, new Sequence(true,
                new AbilityAction(fireBlast, Action.Punch),
                new AbilityAction(fireBlast, Action.Punch),
                new AbilityAction(fireBlast, Action.Sneak),
                new AbilityAction(fireBlast, Action.Punch)
        ));

        sequenceService.registerSequence(jetBlast, new Sequence(true,
                new AbilityAction(fireJet, Action.Sneak),
                new AbilityAction(fireJet, Action.SneakRelease),
                new AbilityAction(fireJet, Action.Sneak),
                new AbilityAction(fireJet, Action.SneakRelease),
                new AbilityAction(fireShield, Action.Sneak),
                new AbilityAction(fireShield, Action.SneakRelease),
                new AbilityAction(fireJet, Action.Punch)
        ));

        sequenceService.registerSequence(jetBlaze, new Sequence(true,
                new AbilityAction(fireJet, Action.Sneak),
                new AbilityAction(fireJet, Action.SneakRelease),
                new AbilityAction(fireJet, Action.Sneak),
                new AbilityAction(fireJet, Action.SneakRelease),
                new AbilityAction(blaze, Action.Sneak),
                new AbilityAction(blaze, Action.SneakRelease),
                new AbilityAction(fireJet, Action.Punch)
        ));

        sequenceService.registerSequence(fireSpin, new Sequence(true,
                new AbilityAction(fireBlast, Action.Punch),
                new AbilityAction(fireBlast, Action.Punch),
                new AbilityAction(fireShield, Action.Punch),
                new AbilityAction(fireShield, Action.Sneak),
                new AbilityAction(fireShield, Action.SneakRelease)
        ));

        sequenceService.registerSequence(fireWheel, new Sequence(true,
                new AbilityAction(fireShield, Action.Sneak),
                new AbilityAction(fireShield, Action.InteractBlock),
                new AbilityAction(fireShield, Action.InteractBlock),
                new AbilityAction(blaze, Action.SneakRelease)
        ));

        sequenceService.registerSequence(airSweep, new Sequence(true,
                new AbilityAction(airSwipe, Action.Punch),
                new AbilityAction(airSwipe, Action.Punch),
                new AbilityAction(airBurst, Action.Sneak),
                new AbilityAction(airBurst, Action.Punch)
        ));

        sequenceService.registerSequence(twister, new Sequence(true,
                new AbilityAction(airShield, Action.Sneak),
                new AbilityAction(airShield, Action.SneakRelease),
                new AbilityAction(tornado, Action.Sneak),
                new AbilityAction(airBlast, Action.Punch)
        ));

        sequenceService.registerSequence(airStream, new Sequence(true,
                new AbilityAction(airShield, Action.Sneak),
                new AbilityAction(airSuction, Action.Punch),
                new AbilityAction(airBlast, Action.Punch)
        ));

        collisionService.registerCollision(airBlast, fireBlast, true, true);
        collisionService.registerCollision(airSwipe, fireBlast, false, true);

        collisionService.registerCollision(airShield, airBlast, false, true);
        collisionService.registerCollision(airShield, airSuction, false, true);
        collisionService.registerCollision(airShield, airStream, false, true);
        collisionService.registerCollision(airShield, fireBlast, false, true);
        collisionService.registerCollision(airShield, fireKick, false, true);
        collisionService.registerCollision(airShield, fireSpin, false, true);
        collisionService.registerCollision(airShield, fireWheel, false, true);

        collisionService.registerCollision(fireBlast, fireBlast, true, true);

        collisionService.registerCollision(fireShield, airBlast, false, true);
        collisionService.registerCollision(fireShield, airSuction, false, true);
        collisionService.registerCollision(fireShield, fireBlast, false, true);
        collisionService.registerCollision(fireShield, fireBlastCharged, false, true);
    }

    private static GenericAbilityDescription registerAbility(String abilityName, Class<? extends Ability> type, Element element, ActivationMethod... activations) {
        GenericAbilityDescription desc = new GenericAbilityDescription<>(abilityName, element, 0, type, activations);
        Game.getAbilityRegistry().registerAbility(desc);
        return desc;
    }
}
