package com.rmb938.bukkit.base.entity.info;

import com.rmb938.bukkit.base.config.WorldConfig;

import java.util.HashMap;

public class WorldInfo {

    private static HashMap<String, WorldInfo> worlds = new HashMap<>();

    public static HashMap<String, WorldInfo> getWorlds() {
        return worlds;
    }
    private final String worldName;
    private final WorldConfig worldConfig;

    public WorldInfo(String worldName, WorldConfig worldConfig) {
        this.worldName = worldName;
        this.worldConfig = worldConfig;
    }

    public String getWorldName() {
        return worldName;
    }

    public WorldConfig getWorldConfig() {
        return worldConfig;
    }
}
