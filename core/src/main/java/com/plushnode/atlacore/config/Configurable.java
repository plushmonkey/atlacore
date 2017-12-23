package com.plushnode.atlacore.config;

import java.util.Observable;
import java.util.Observer;

public abstract class Configurable implements Observer {
    protected Configuration config;

    public Configurable() {
        this.config = ConfigManager.getInstance().getConfig();
        ConfigManager.getInstance().addObserver(this);
    }

    public void destroy() {
        ConfigManager.getInstance().deleteObserver(this);
    }

    @Override
    public final void update(Observable o, Object arg) {
        this.config = (Configuration)arg;
        onConfigReload();
    }

    public abstract void onConfigReload();
}
