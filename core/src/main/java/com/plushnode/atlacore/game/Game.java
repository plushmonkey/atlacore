package com.plushnode.atlacore.game;

import com.google.common.reflect.ClassPath;
import com.plushnode.atlacore.CorePlugin;
import com.plushnode.atlacore.collision.CollisionService;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.game.ability.*;
import com.plushnode.atlacore.game.ability.air.*;
import com.plushnode.atlacore.game.ability.air.passives.AirAgility;
import com.plushnode.atlacore.game.ability.earth.Shockwave;
import com.plushnode.atlacore.game.ability.fire.*;
import com.plushnode.atlacore.game.ability.fire.sequences.*;
import com.plushnode.atlacore.game.ability.sequence.AbilityAction;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.ability.sequence.Sequence;
import com.plushnode.atlacore.game.ability.sequence.SequenceService;
import com.plushnode.atlacore.game.element.BasicElement;
import com.plushnode.atlacore.game.element.ElementRegistry;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.player.*;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.protection.ProtectionSystem;
import com.plushnode.atlacore.store.sql.DatabaseManager;
import com.plushnode.atlacore.util.ChatColor;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.block.TempBlockService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class Game {
    public static CorePlugin plugin;

    private static PlayerService playerService;
    private static ProtectionSystem protectionSystem;

    private static AbilityRegistry abilityRegistry;
    private static ElementRegistry elementRegistry;
    private static AbilityInstanceManager instanceManager;
    private static TempBlockService tempBlockService;
    private static SequenceService sequenceService;
    private static CollisionService collisionService;

    public Game(CorePlugin plugin) {
        Game.plugin = plugin;

        instanceManager = new AbilityInstanceManager();
        abilityRegistry = new AbilityRegistry();
        protectionSystem = new ProtectionSystem();
        tempBlockService = new TempBlockService();
        elementRegistry = new ElementRegistry();
        sequenceService = new SequenceService();
        collisionService = new CollisionService();

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

        elementRegistry.registerElement(Elements.AIR);
        elementRegistry.registerElement(Elements.EARTH);
        elementRegistry.registerElement(Elements.FIRE);
        elementRegistry.registerElement(Elements.WATER);

        loadAbilities();

        if (startup) {
            Game.playerService = new PlayerService(loadPlayerRepository());
        } else {
            plugin.loadConfig();
            Game.getPlayerService().reload(loadPlayerRepository());
        }

        for (Player player : playerService.getOnlinePlayers()) {
            getAbilityInstanceManager().createPassives(player);
        }
    }

    private static void loadAbilities() {
        AbilityDescription blazeDesc = new GenericAbilityDescription<>("Blaze", "Blaze it 420",
                Elements.FIRE, 3000,
                Arrays.asList(ActivationMethod.Punch, ActivationMethod.Sneak), Blaze.class, false);

        AbilityDescription scooterDesc = new GenericAbilityDescription<>("AirScooter", "scoot scoot",
                Elements.AIR, 3000,
                Arrays.asList(ActivationMethod.Punch), AirScooter.class, true);

        AbilityDescription shockwaveDesc = new GenericAbilityDescription<>("Shockwave", "wave wave",
                Elements.EARTH, 6000,
                Arrays.asList(ActivationMethod.Punch, ActivationMethod.Sneak, ActivationMethod.Fall), Shockwave.class, false);

        AbilityDescription airSwipeDesc = new GenericAbilityDescription<>("AirSwipe", "swipe swipe",
                Elements.AIR, 1500,
                Arrays.asList(ActivationMethod.Punch, ActivationMethod.Sneak), AirSwipe.class, false);

        AbilityDescription airBlastDesc = new GenericAbilityDescription<>("AirBlast", "blast blast",
                Elements.AIR, 500,
                Arrays.asList(ActivationMethod.Punch, ActivationMethod.Sneak), AirBlast.class, false);

        AbilityDescription fireBlastDesc = new GenericAbilityDescription<>("FireBlast", "fire blast blast",
                Elements.FIRE, 1500,
                Arrays.asList(ActivationMethod.Punch, ActivationMethod.Sneak), FireBlast.class, false);

        AbilityDescription fireJetDesc = new GenericAbilityDescription<>("FireJet", "jet jet",
                Elements.FIRE, 7000,
                Arrays.asList(ActivationMethod.Punch), FireJet.class, true);

        AbilityDescription fireShieldDesc = new GenericAbilityDescription<>("FireShield", "fire shield shield",
                Elements.FIRE, 100,
                Arrays.asList(ActivationMethod.Punch, ActivationMethod.Sneak), FireShield.class, false);

        AbilityDescription wallOfFireDesc = new GenericAbilityDescription<>("WallOfFire", "wall of fire",
                Elements.FIRE, 100,
                Arrays.asList(ActivationMethod.Punch), WallOfFire.class, false);

        AbilityDescription heatControlDesc = new GenericAbilityDescription<>("HeatControl", "controls heat",
                Elements.FIRE, 500,
                Arrays.asList(ActivationMethod.Punch), HeatControl.class, false);

        AbilityDescription lightningDesc = new GenericAbilityDescription<>("Lightning", "shoot lightning",
                Elements.FIRE, 500,
                Arrays.asList(ActivationMethod.Sneak), Lightning.class, false);

        AbilityDescription combustionDesc = new GenericAbilityDescription<>("Combustion", "combust",
                Elements.FIRE, 500,
                Arrays.asList(ActivationMethod.Sneak), Combustion.class, false);

        AbilityDescription fireBurstDesc = new GenericAbilityDescription<>("FireBurst", "burst fire",
                Elements.FIRE, 500,
                Arrays.asList(ActivationMethod.Sneak), FireBurst.class, false);

        AbilityDescription airShieldDesc = new GenericAbilityDescription<>("AirShield", "shield air",
                Elements.AIR, 500,
                Arrays.asList(ActivationMethod.Sneak), AirShield.class, false);

        AbilityDescription airSpoutDesc = new GenericAbilityDescription<>("AirSpout", "air spout",
                Elements.AIR, 500,
                Arrays.asList(ActivationMethod.Punch), AirSpout.class, true);

        AbilityDescription fireKickDesc = new GenericAbilityDescription<>("FireKick", "kick kick",
                Elements.FIRE, 1500,
                Arrays.asList(ActivationMethod.Sequence), FireKick.class, false);

        AbilityDescription jetBlastDesc = new GenericAbilityDescription<>("JetBlast", "jet blast blast",
                Elements.FIRE, 6000,
                Arrays.asList(ActivationMethod.Sequence), JetBlast.class, true);

        AbilityDescription jetBlazeDesc = new GenericAbilityDescription<>("JetBlaze", "jet blaze blaze",
                Elements.FIRE, 6000,
                Arrays.asList(ActivationMethod.Sequence), JetBlaze.class, false);

        AbilityDescription fireSpinDesc = new GenericAbilityDescription<>("FireSpin", "firespin",
                Elements.FIRE, 5000,
                Arrays.asList(ActivationMethod.Sequence), FireSpin.class, false);

        AbilityDescription fireWheelDesc = new GenericAbilityDescription<>("FireWheel", "fire wheel",
                Elements.FIRE, 5000,
                Arrays.asList(ActivationMethod.Sequence), FireWheel.class, false);

        AbilityDescription airAgilityDesc = new GenericAbilityDescription<>("AirAgility", "air agility",
                Elements.AIR, 5000,
                Arrays.asList(ActivationMethod.Passive), AirAgility.class, true);
        airAgilityDesc.setHidden(true);

        abilityRegistry.registerAbility(blazeDesc);
        abilityRegistry.registerAbility(scooterDesc);
        abilityRegistry.registerAbility(shockwaveDesc);
        abilityRegistry.registerAbility(airSwipeDesc);
        abilityRegistry.registerAbility(airBlastDesc);
        abilityRegistry.registerAbility(fireBlastDesc);
        abilityRegistry.registerAbility(fireJetDesc);
        abilityRegistry.registerAbility(fireShieldDesc);
        abilityRegistry.registerAbility(wallOfFireDesc);
        abilityRegistry.registerAbility(heatControlDesc);
        abilityRegistry.registerAbility(lightningDesc);
        abilityRegistry.registerAbility(combustionDesc);
        abilityRegistry.registerAbility(fireBurstDesc);
        abilityRegistry.registerAbility(airShieldDesc);
        abilityRegistry.registerAbility(airSpoutDesc);

        abilityRegistry.registerAbility(fireKickDesc);
        abilityRegistry.registerAbility(jetBlastDesc);
        abilityRegistry.registerAbility(jetBlazeDesc);
        abilityRegistry.registerAbility(fireSpinDesc);
        abilityRegistry.registerAbility(fireWheelDesc);

        abilityRegistry.registerAbility(airAgilityDesc);

        sequenceService.registerSequence(fireKickDesc, new Sequence(true,
                new AbilityAction(fireBlastDesc, Action.Punch),
                new AbilityAction(fireBlastDesc, Action.Punch),
                new AbilityAction(fireBlastDesc, Action.Sneak),
                new AbilityAction(fireBlastDesc, Action.Punch)
        ));

        sequenceService.registerSequence(jetBlastDesc, new Sequence(true,
                new AbilityAction(fireJetDesc, Action.Sneak),
                new AbilityAction(fireJetDesc, Action.SneakRelease),
                new AbilityAction(fireJetDesc, Action.Sneak),
                new AbilityAction(fireJetDesc, Action.SneakRelease),
                new AbilityAction(fireShieldDesc, Action.Sneak),
                new AbilityAction(fireShieldDesc, Action.SneakRelease),
                new AbilityAction(fireJetDesc, Action.Punch)
        ));

        sequenceService.registerSequence(jetBlazeDesc, new Sequence(true,
                new AbilityAction(fireJetDesc, Action.Sneak),
                new AbilityAction(fireJetDesc, Action.SneakRelease),
                new AbilityAction(fireJetDesc, Action.Sneak),
                new AbilityAction(fireJetDesc, Action.SneakRelease),
                new AbilityAction(blazeDesc, Action.Sneak),
                new AbilityAction(blazeDesc, Action.SneakRelease),
                new AbilityAction(fireJetDesc, Action.Punch)
        ));

        sequenceService.registerSequence(fireSpinDesc, new Sequence(true,
                new AbilityAction(fireBlastDesc, Action.Punch),
                new AbilityAction(fireBlastDesc, Action.Punch),
                new AbilityAction(fireShieldDesc, Action.Punch),
                new AbilityAction(fireShieldDesc, Action.Sneak),
                new AbilityAction(fireShieldDesc, Action.SneakRelease)
        ));

        sequenceService.registerSequence(fireWheelDesc, new Sequence(true,
                new AbilityAction(fireShieldDesc, Action.Sneak),
                new AbilityAction(fireShieldDesc, Action.InteractBlock),
                new AbilityAction(fireShieldDesc, Action.InteractBlock),
                new AbilityAction(blazeDesc, Action.SneakRelease)
        ));

        collisionService.registerCollision(airBlastDesc, fireBlastDesc, true, true);
        collisionService.registerCollision(airSwipeDesc, fireBlastDesc, false, true);
        collisionService.registerCollision(fireShieldDesc, fireBlastDesc, false, true);

        collisionService.registerCollision(airShieldDesc, fireBlastDesc, false, true);
        collisionService.registerCollision(airShieldDesc, airBlastDesc, false, true);
        collisionService.registerCollision(airShieldDesc, fireKickDesc, false, true);
        collisionService.registerCollision(airShieldDesc, fireSpinDesc, false, true);
        collisionService.registerCollision(airShieldDesc, fireWheelDesc, false, true);

        Elements.AIR.getPassives().clear();
        Elements.AIR.addPassive(Game.getAbilityRegistry().getAbilityByName("AirAgility"));
    }

    private static PlayerRepository loadPlayerRepository() {
        CommentedConfigurationNode configRoot = ConfigManager.getInstance().getConfig();

        String engine = configRoot.getNode("storage").getNode("engine").getString("sqlite");

        DatabaseManager databaseManager;

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

    public void addAbility(User user, Ability instance) {
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
