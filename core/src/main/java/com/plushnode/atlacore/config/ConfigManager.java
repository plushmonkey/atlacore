package com.plushnode.atlacore.config;


import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Observable;

public class ConfigManager extends Observable {
    private static ConfigManager instance;
    private CommentedConfigurationNode config = null;

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        if (instance == null)
            instance = new ConfigManager();
        return instance;
    }

    public void setConfig(CommentedConfigurationNode node) {
        this.config = node;
    }

    public CommentedConfigurationNode getConfig() {
        return config;
    }

    public void onConfigReload() {
        setChanged();
        notifyObservers(config);
    }
}
