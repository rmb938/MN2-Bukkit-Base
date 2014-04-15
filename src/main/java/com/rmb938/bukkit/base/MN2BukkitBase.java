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
import com.rmb938.jedis.net.command.server.NetCommandSTSC;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.logging.Level;

public class MN2BukkitBase extends JavaPlugin {

    private UserLoader userLoader;
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

        DatabaseAPI.initializeMongo(serverConfig.mongo_database, serverConfig.mongo_address, serverConfig.mongo_port);

        JedisManager.connectToRedis(serverConfig.redis_address);
        JedisManager.setUpDelegates();

        new NetCommandHandlerBTS(this);
        new NetCommandHandlerSCTS(this);

        try {
            Jedis jedis = JedisManager.getJedis();
            String key = getServer().getIp() + "." + getServer().getPort();
            serverUUID = jedis.get(key + ".uuid");
            jedis.del(getServer().getIp() + "." + getServer().getPort());
            jedis.del(getServer().getIp() + "." + getServer().getPort() + ".uuid");
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
                updateServer();
                sendHeartbeat();
            }
        }, 200L, 200L);
    }

    @Override
    public void onDisable() {
        Jedis jedis = JedisManager.getJedis();
        while (jedis.setnx("lock." + getServer().getServerName().split("\\.")[0] + ".key", System.currentTimeMillis() + 30000 + "") == 0) {
            String lock = jedis.get("lock." + getServer().getServerName().split("\\.")[0] + ".key");
            long time = Long.parseLong(lock != null ? lock : "0");
            if (System.currentTimeMillis() > time) {
                try {
                    time = Long.parseLong(jedis.getSet("lock." + getServer().getServerName().split("\\.")[0] + ".key", System.currentTimeMillis() + 30000 + ""));
                } catch (Exception ex) {
                    time = 0;
                }
                if (System.currentTimeMillis() < time) {
                    continue;
                }
            } else {
                continue;
            }
            break;
        }
        String uuid = jedis.get("server." + getServer().getServerName());
        if (uuid.equals(serverUUID)) {
            jedis.del("server." + getServer().getServerName());
        }
        jedis.del("lock." + getServer().getServerName().split("\\.")[0] + ".key");
        JedisManager.returnJedis(jedis);

        removeServer();

        JedisManager.shutDown();
    }

    public String getServerUUID() {
        return serverUUID;
    }


    public UserLoader getUserLoader() {
        return userLoader;
    }

    private void updateServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("updateServer", serverUUID);
        netCommandSTB.addArg("IP", getServer().getIp());
        netCommandSTB.addArg("port", getServer().getPort());
        netCommandSTB.addArg("serverName", getServer().getServerName());
        netCommandSTB.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void sendHeartbeat() {
        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("heartbeat", getServer().getPort(), getServer().getIp());
        netCommandSTSC.addArg("serverName", getServer().getServerName().split("\\.")[0]);
        netCommandSTSC.addArg("serverUUID", serverUUID);
        netCommandSTSC.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTSC.flush();
        Jedis jedis = JedisManager.getJedis();
        String uuid = jedis.get("server." + getServer().getServerName());
        if (uuid.equals(serverUUID)) {
            jedis.expire("server." + getServer().getServerName(), 60);
        } else {
            getLogger().severe("UUID doesn't match jedis. Shutting Down.");
            getServer().shutdown();
        }
        JedisManager.returnJedis(jedis);
    }

    public void removeServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("removeServer", serverUUID);
        netCommandSTB.flush();

        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("removeServer", getServer().getPort(), getServer().getIp());
        netCommandSTSC.addArg("serverUUID", serverUUID);
        netCommandSTSC.flush();
    }

}
