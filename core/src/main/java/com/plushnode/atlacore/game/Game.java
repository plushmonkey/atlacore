package com.plushnode.atlacore.game;

import com.google.common.reflect.ClassPath;
import com.plushnode.atlacore.CorePlugin;
import com.plushnode.atlacore.block.StandardTempBlockService;
import com.plushnode.atlacore.collision.CollisionService;
import com.plushnode.atlacore.config.ConfigManager;
import com.plushnode.atlacore.game.ability.*;
import com.plushnode.atlacore.game.ability.sequence.SequenceService;
import com.plushnode.atlacore.game.attribute.AttributeSystem;
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
        elementRegistry = new ElementRegistry();
        tempBlockService = new StandardTempBlockService();
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

    public static void reload() {
        reload(false);
    }

    private static void reload(boolean startup) {
        getAbilityInstanceManager().destroyAllInstances();
        abilityRegistry.clear();
        elementRegistry.clear();
        sequenceService.clear();
        collisionService.clear();

        if (tempBlockService != null) {
            tempBlockService.resetAll();
        }

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

    public static void cleanup() {
        getAbilityInstanceManager().destroyAllInstances();

        getTempArmorService().reload();
        getTempBlockService().resetAll();

        Flight.removeAll();
    }

    private static void loadAbilities() {
        AbilityInitializer.loadAbilities();

        Elements.AIR.getPassives().clear();
        Elements.EARTH.getPassives().clear();
        Elements.FIRE.getPassives().clear();
        Elements.WATER.getPassives().clear();

        Elements.AIR.addPassive(Game.getAbilityRegistry().getAbilityByName("AirAgility"));
        Elements.AIR.addPassive(Game.getAbilityRegistry().getAbilityByName("GracefulDescent"));

        Elements.EARTH.addPassive(Game.getAbilityRegistry().getAbilityByName("DensityShift"));
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
