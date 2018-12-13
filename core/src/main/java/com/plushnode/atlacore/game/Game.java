package com.plushnode.atlacore.game;

import com.google.common.reflect.ClassPath;
import com.plushnode.atlacore.CorePlugin;
import com.plushnode.atlacore.collision.CollisionService;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.game.ability.*;
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
import com.plushnode.atlacore.game.ability.water.arms.WaterArms;
import com.plushnode.atlacore.game.ability.water.arms.WaterArmsFreeze;
import com.plushnode.atlacore.game.ability.water.arms.WaterArmsSpear;
import com.plushnode.atlacore.game.ability.water.arms.WaterArmsWhip;
import com.plushnode.atlacore.game.ability.water.surge.Surge;
import com.plushnode.atlacore.game.ability.water.surge.SurgeWall;
import com.plushnode.atlacore.game.ability.water.surge.SurgeWave;
import com.plushnode.atlacore.game.ability.water.torrent.Torrent;
import com.plushnode.atlacore.game.ability.water.torrent.TorrentWave;
import com.plushnode.atlacore.game.ability.water.util.BottleReturn;
import com.plushnode.atlacore.game.attribute.AttributeSystem;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.game.element.ElementRegistry;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.player.*;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.preset.MemoryPresetRepository;
import com.plushnode.atlacore.preset.PresetService;
import com.plushnode.atlacore.preset.SqlPresetRepository;
import com.plushnode.atlacore.protection.ProtectionSystem;
import com.plushnode.atlacore.store.sql.DatabaseManager;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.block.TempBlockService;
import com.plushnode.atlacore.util.TempArmorService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class Game {
    public static CorePlugin plugin;

    private static PlayerService playerService;
    private static PresetService presetService;
    private static ProtectionSystem protectionSystem;

    private static AbilityRegistry abilityRegistry;
    private static ElementRegistry elementRegistry;
    private static AbilityInstanceManager instanceManager;
    private static TempBlockService tempBlockService;
    private static SequenceService sequenceService;
    private static CollisionService collisionService;
    private static AttributeSystem attributeSystem;
    private static TempArmorService tempArmorService;
    private static ActivationController activationController;

    private static DatabaseManager databaseManager = null;

    public Game(CorePlugin plugin) {
        Game.plugin = plugin;

        instanceManager = new AbilityInstanceManager();
        abilityRegistry = new AbilityRegistry();
        protectionSystem = new ProtectionSystem();
        tempBlockService = new TempBlockService();
        elementRegistry = new ElementRegistry();
        sequenceService = new SequenceService();
        collisionService = new CollisionService();
        attributeSystem = new AttributeSystem();
        tempArmorService = new TempArmorService();
        activationController = new ActivationController();

        tempBlockService.start();
        sequenceService.start();
        collisionService.start();

        initializeClasses();

        plugin.createTaskTimer(this::update, 1, 1);
        plugin.createTaskTimer(Flight::updateAll, 1, 1);

        reload(true);

        for (Player player : playerService.getOnlinePlayers()) {
            getAbilityInstanceManager().createPassives(player);
        }
    }

    public static void reload() {
        reload(false);
    }

    private static void reload(boolean startup) {
        getAbilityInstanceManager().destroyAllInstances();
        abilityRegistry.clear();
        elementRegistry.clear();
        sequenceService.clear();
        collisionService.clear();
        tempBlockService.resetAll();
        tempArmorService.reload();

        elementRegistry.registerElement(Elements.AIR);
        elementRegistry.registerElement(Elements.EARTH);
        elementRegistry.registerElement(Elements.FIRE);
        elementRegistry.registerElement(Elements.WATER);

        loadAbilities();

        if (startup) {
            Game.playerService = new PlayerService(loadPlayerRepository());

            if (databaseManager != null) {
                Game.presetService = new PresetService(new SqlPresetRepository(databaseManager));
            } else {
                Game.presetService = new PresetService(new MemoryPresetRepository());
            }
        } else {
            plugin.loadConfig();
            Game.getPlayerService().reload(loadPlayerRepository());
            if (databaseManager != null) {
                Game.presetService = new PresetService(new SqlPresetRepository(databaseManager));
            } else {
                Game.presetService = new PresetService(new MemoryPresetRepository());
            }
        }

        for (Player player : playerService.getOnlinePlayers()) {
            getAbilityInstanceManager().createPassives(player);
        }
    }

    private static void loadAbilities() {
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

        GenericAbilityDescription waterArmsDesc = registerAbility("WaterArms", WaterArms.class, Elements.WATER, ActivationMethod.Punch, ActivationMethod.Sneak);
        waterArmsDesc.setCanBypassCooldown(true);
        waterArmsDesc.setSourcesPlants(true);

        MultiAbilityDescription freezeDesc = new MultiAbilityDescription<>("Freeze", Elements.WATER, 0, WaterArmsFreeze.class, ActivationMethod.Punch);
        freezeDesc.setConfigNode("waterarms", "freeze");
        freezeDesc.setHidden(true);
        abilityRegistry.registerAbility(freezeDesc);

        MultiAbilityDescription spearDesc = new MultiAbilityDescription<>("Spear", Elements.WATER, 0, WaterArmsSpear.class, ActivationMethod.Punch);
        spearDesc.setConfigNode("waterarms", "spear");
        spearDesc.setHidden(true);
        abilityRegistry.registerAbility(spearDesc);

        MultiAbilityDescription whipDesc = new MultiAbilityDescription<>("Whip", Elements.WATER, 0, WaterArmsWhip.class, ActivationMethod.Punch);
        whipDesc.setConfigNode("waterarms", "whip");
        whipDesc.setHidden(true);
        abilityRegistry.registerAbility(whipDesc);

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

        Elements.AIR.getPassives().clear();
        Elements.EARTH.getPassives().clear();
        Elements.FIRE.getPassives().clear();
        Elements.WATER.getPassives().clear();

        Elements.AIR.addPassive(Game.getAbilityRegistry().getAbilityByName("AirAgility"));
        Elements.AIR.addPassive(Game.getAbilityRegistry().getAbilityByName("GracefulDescent"));

        Elements.EARTH.addPassive(Game.getAbilityRegistry().getAbilityByName("DensityShift"));
    }

    private static GenericAbilityDescription registerAbility(String abilityName, Class<? extends Ability> type, Element element, ActivationMethod... activations) {
        GenericAbilityDescription desc = new GenericAbilityDescription<>(abilityName, element, 0, type, activations);
        abilityRegistry.registerAbility(desc);
        return desc;
    }

    private static PlayerRepository loadPlayerRepository() {
        CommentedConfigurationNode configRoot = ConfigManager.getInstance().getConfig();

        String engine = configRoot.getNode("storage").getNode("engine").getString("sqlite");

        CommentedConfigurationNode mysqlNode = configRoot.getNode("storage").getNode("mysql");

        // Initialize config with mysql values.
        String host = mysqlNode.getNode("host").getString("localhost");
        int port = mysqlNode.getNode("port").getInt(3306);
        String username = mysqlNode.getNode("username").getString("bending");
        String password = mysqlNode.getNode("password").getString("password");
        String database = mysqlNode.getNode("database").getString("bending");

        PlayerRepository repository = null;

        if ("sqlite".equalsIgnoreCase(engine)) {
            String databasePath = plugin.getConfigFolder() + "/atlacore.db";

            try {
                databaseManager = new DatabaseManager("jdbc:sqlite:" + databasePath, "", "", "", "org.sqlite.JDBC");
                databaseManager.initDatabase("sqlite.sql");

                repository = new SqlPlayerRepository(databaseManager, plugin.getPlayerFactory());
                ((SqlPlayerRepository)repository).createElements(Game.getElementRegistry().getElements());
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        } else if ("mysql".equalsIgnoreCase(engine)) {
            try {
                databaseManager = new DatabaseManager(
                        "jdbc:mysql://" + host + ":" + port + "/",
                        database,
                        username,
                        password,
                        "com.mysql.jdbc.Driver"
                );

                databaseManager.initDatabase("mysql.sql");

                repository = new SqlPlayerRepository(databaseManager, plugin.getPlayerFactory());
                ((SqlPlayerRepository)repository).createElements(Game.getElementRegistry().getElements());
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        }

        if (repository == null) {
            repository = new MemoryPlayerRepository(plugin.getPlayerFactory());
            plugin.warn("Failed to load storage engine. Defaulting to in-memory engine.");
        }

        return repository;
    }

    public AbilityDescription getAbilityDescription(String abilityName) {
        return abilityRegistry.getAbilityByName(abilityName);
    }

    public static void addAbility(User user, Ability instance) {
        instanceManager.addAbility(user, instance);
    }

    public void update() {
        instanceManager.update();

        updateCooldowns();
    }

    private void updateCooldowns() {
        long time = System.currentTimeMillis();

        for (Player player : Game.getPlayerService().getOnlinePlayers()) {
            Map<AbilityDescription, Long> cooldowns = player.getCooldowns();

            for (Iterator<Map.Entry<AbilityDescription, Long>> iterator = cooldowns.entrySet().iterator();
                 iterator.hasNext();)
            {
                Map.Entry<AbilityDescription, Long> entry = iterator.next();

                if (time >= entry.getValue()) {
                    Game.plugin.getEventBus().postCooldownRemoveEvent(player, entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    public static SequenceService getSequenceService() {
        return sequenceService;
    }

    public static CollisionService getCollisionService() {
        return collisionService;
    }

    public static PlayerService getPlayerService() {
        return playerService;
    }

    public static PresetService getPresetService() {
        return presetService;
    }

    public static ProtectionSystem getProtectionSystem() {
        return protectionSystem;
    }

    public static void setProtectionSystem(ProtectionSystem protectionSystem) {
        Game.protectionSystem = protectionSystem;
    }

    public static AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    public static ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    public static AbilityInstanceManager getAbilityInstanceManager() {
        return instanceManager;
    }

    public static TempBlockService getTempBlockService() {
        return tempBlockService;
    }

    public static AttributeSystem getAttributeSystem() {
        return attributeSystem;
    }

    public static TempArmorService getTempArmorService() {
        return tempArmorService;
    }

    public static ActivationController getActivationController() {
        return activationController;
    }

    // Forces all atlacore classes to be loaded. This ensures all of them create their static Config objects.
    // Creating the config objects forces them to fill out their default values, which get saved after game is created.
    private void initializeClasses() {
        try {
            ClassPath cp = ClassPath.from(Game.class.getClassLoader());

            for (ClassPath.ClassInfo info : cp.getTopLevelClassesRecursive("com.plushnode.atlacore")) {
                if (info.getPackageName().contains("internal")) continue;

                try {
                    Class.forName(info.getName());
                } catch (ClassNotFoundException e) {

                }
            }
        } catch (IOException e) {

        }
    }

    public static void info(String message) {
        plugin.info(message);
    }

    public static void warn(String message) {
        plugin.warn(message);
    }
}
