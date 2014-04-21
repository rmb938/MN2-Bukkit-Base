package com.rmb938.bukkit.base;

import com.rmb938.bukkit.base.config.MainConfig;
import com.rmb938.bukkit.base.config.WorldConfig;
import com.rmb938.bukkit.base.database.UserLoader;
import com.rmb938.bukkit.base.entity.info.WorldInfo;
import com.rmb938.bukkit.base.jedis.NetCommandHandlerBTS;
import com.rmb938.bukkit.base.jedis.NetCommandHandlerSCTS;
import com.rmb938.bukkit.base.listeners.PlayerListener;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.command.server.NetCommandSTB;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.logging.Level;

public class MN2BukkitBase extends JavaPlugin {

    private static UserLoader userLoader;

    public static UserLoader getUserLoader() {
        return userLoader;
    }

    private String serverUUID;

    @Override
    public void onEnable() {
        getLogger().warning("--------------------------------------------------");
        getLogger().warning("Multi-Node Minecraft Network is under the Creative Commons");
        getLogger().warning("Attribution-NonCommercial 4.0 International Public License");
        getLogger().warning("If you are using this in a commercial environment you MUST");
        getLogger().warning("obtain written permission.");
        getLogger().warning("--------------------------------------------------");
        MainConfig serverConfig = new MainConfig(this);
        try {
            serverConfig.init();
            serverConfig.save();
        } catch (net.cubespace.Yamler.Config.InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null, e);
            return;
        }

        for (File worldFolder : getServer().getWorldContainer().listFiles()) {
            if (worldFolder.isDirectory() == false) {
                continue;
            }
            File worldConfigFile = new File(worldFolder, "config.yml");
            WorldConfig worldConfig = new WorldConfig(worldConfigFile, worldFolder.getName());
            try {
                worldConfig.init();
            } catch (InvalidConfigurationException e) {
                getLogger().severe("Error loading world config for " + worldConfigFile.getName());
                getLogger().log(Level.SEVERE, null, e);
                continue;
            }
            WorldInfo worldInfo = new WorldInfo(worldFolder.getName(), worldConfig);
            WorldInfo.getWorlds().put(worldInfo.getWorldName(), worldInfo);
        }
        try {
            DatabaseAPI.initializeMongo(serverConfig.mongo_database, serverConfig.mongo_address, serverConfig.mongo_port);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to mongo. Closing");
            getServer().shutdown();
            return;
        }

        JedisManager.connectToRedis(serverConfig.redis_address);
        JedisManager.setUpDelegates();

        new NetCommandHandlerBTS(this);
        new NetCommandHandlerSCTS(this);

        try {
            Jedis jedis = JedisManager.getJedis();
            String data = jedis.get("server." + getServer().getServerName());
            JSONObject jsonObject = new JSONObject(data);
            serverUUID = jsonObject.getString("uuid");
            JedisManager.returnJedis(jedis);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to redis. Closing");
            getServer().shutdown();
            return;
        }

        userLoader = new UserLoader(this);
        new PlayerListener(this);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                addServer();
                sendHeartbeat();
            }
        }, 200L, 200L);
    }

    @Override
    public void onDisable() {
        Jedis jedis = JedisManager.getJedis();
        String data = jedis.get("server." + getServer().getServerName());
        try {
            JSONObject jsonObject = new JSONObject(data);
            String uuid = jsonObject.getString("uuid");
            if (uuid.equals(serverUUID)) {
                jedis.del("server." + getServer().getServerName());
            }
        } catch (JSONException e) {
            getLogger().severe("Error getting JSON server info.");
        } finally {
            JedisManager.returnJedis(jedis);
        }

        removeServer();

        JedisManager.shutDown();
    }

    public String getServerUUID() {
        return serverUUID;
    }

    private void addServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("addServer", serverUUID);
        netCommandSTB.addArg("IP", getServer().getIp());
        netCommandSTB.addArg("port", getServer().getPort());
        netCommandSTB.addArg("serverName", getServer().getServerName());
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void sendHeartbeat() {
        Jedis jedis = JedisManager.getJedis();
        String data = jedis.get("server." + getServer().getServerName());
        try {
            JSONObject jsonObject = new JSONObject(data);
            String uuid = jsonObject.getString("uuid");
            if (uuid.equals(serverUUID)) {
                if (getServer().getOnlinePlayers().length == 0) {
                    jsonObject.put("timeEmpty", jsonObject.getInt("timeEmpty") + 10);
                } else {
                    jsonObject.put("timeEmpty", 0);
                }
                jsonObject.put("currentPlayers", getServer().getOnlinePlayers().length);
                jedis.set("server." + getServer().getServerName(), jsonObject.toString());
                jedis.expire("server." + getServer().getServerName(), 60);
            } else {
                getLogger().severe("UUID doesn't match jedis. Shutting Down.");
                getServer().shutdown();
            }
        } catch (JSONException e) {
            getLogger().severe("Error getting JSON server info. Shutting down");
            getServer().shutdown();
        } finally {
            JedisManager.returnJedis(jedis);
        }
    }

    public void removeServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("removeServer", serverUUID);
        netCommandSTB.flush();
    }

}
