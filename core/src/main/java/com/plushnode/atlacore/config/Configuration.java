package com.plushnode.atlacore.config;

import java.util.List;

public interface Configuration {
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);
    List<Boolean> getBooleanList(String path);
    List<Byte> getByteList(String path);
    List<Character> getCharacterList(String path);
    Configuration getConfigurationSection(String path);
    long getLong(String path);
    long getLong(String path, long def);
    double getDouble(String path);
    double getDouble(String path, double def);
}
