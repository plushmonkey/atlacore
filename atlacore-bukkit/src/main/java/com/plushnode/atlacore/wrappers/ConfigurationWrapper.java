package com.plushnode.atlacore.wrappers;

import com.plushnode.atlacore.config.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class ConfigurationWrapper implements Configuration {
    private ConfigurationSection section;

    public ConfigurationWrapper(ConfigurationSection section) {
        this.section = section;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfigurationWrapper) {
            return section.equals(((ConfigurationWrapper)obj).section);
        }
        return section.equals(obj);
    }

    @Override
    public int hashCode() {
        return section.hashCode();
    }

    @Override
    public boolean getBoolean(String path) {
        return section.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return section.getBoolean(path, def);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return section.getBooleanList(path);
    }

    @Override
    public List<Byte> getByteList(String path) {
        return section.getByteList(path);
    }

    @Override
    public List<Character> getCharacterList(String path) {
        return section.getCharacterList(path);
    }

    @Override
    public Configuration getConfigSection(String path) {
        return new ConfigurationWrapper(section.getConfigurationSection(path));
    }

    @Override
    public long getLong(String path) {
        return section.getLong(path);
    }

    @Override
    public long getLong(String path, long def) {
        return section.getLong(path, def);
    }
}
