package com.plushnode.atlacore.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Observable;
import java.util.Observer;

public abstract class Configurable implements Observer, Cloneable {
    protected CommentedConfigurationNode config;

    public Configurable() {
        this.config = ConfigManager.getInstance().getConfig();
        ConfigManager.getInstance().addObserver(this);

        if (this.config != null) {
            onConfigReload();
        }
    }

    public void destroy() {
        ConfigManager.getInstance().deleteObserver(this);
    }

    @Override
    public final void update(Observable o, Object arg) {
        this.config = (CommentedConfigurationNode)arg;
        onConfigReload();
    }

    public abstract void onConfigReload();

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
