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
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

public class MN2BukkitBase extends JavaPlugin {

    private UserLoader userLoader;
    private MainConfig serverConfig;
    private String serverName;
    private String serverUUID;
    private int serverNumber;

    @Override
    public void onEnable() {
        serverConfig = new MainConfig(this);
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
                getLogger().severe("Error loading world config for "+worldConfigFile.getName());
                getLogger().log(Level.SEVERE, null, e);
                continue;
            }
            WorldInfo worldInfo = new WorldInfo(worldFolder.getName(), worldConfig);
            WorldInfo.getWorlds().put(worldInfo.getWorldName(), worldInfo);
        }

        DatabaseAPI.initializeMySQL(serverConfig.mySQL_userName, serverConfig.mySQL_password, serverConfig.mySQL_database, serverConfig.mySQL_address, serverConfig.mySQL_port);

        JedisManager.connectToRedis(serverConfig.redis_address);
        JedisManager.setUpDelegates();

        new NetCommandHandlerBTS(this);
        new NetCommandHandlerSCTS(this);

        try {
            Jedis jedis = JedisManager.getJedis();
            String key = getServer().getIp()+"."+getServer().getPort();
            serverName = jedis.get(key);
            serverUUID = jedis.get(key+".uuid");
            jedis.del(getServer().getIp()+"."+getServer().getPort());
            jedis.del(getServer().getIp()+"."+getServer().getPort()+".uuid");
            JedisManager.returnJedis(jedis);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to redis. Closing");
            getServer().shutdown();
            return;
        }

        Jedis jedis = JedisManager.getJedis();
        while (jedis.setnx("lock." + serverName+".key", System.currentTimeMillis() + 30000 + "") == 0) {
            String lock = jedis.get("lock." + serverName+".key");
            long time = Long.parseLong(lock != null ? lock : "0");
            if (System.currentTimeMillis() > time) {
                time = Long.parseLong(jedis.getSet("lock." + serverName+".key", System.currentTimeMillis() + 30000 + ""));
                if (System.currentTimeMillis() < time) {
                    continue;
                }
            } else {
                continue;
            }
            break;
        }

        Set<String> keys = jedis.keys("server." + serverName + ".*");
        ArrayList<Integer> ids = new ArrayList<>();
        int startId = 1;
        for (String keyName : keys) {
            int id = Integer.parseInt(keyName.split("\\.")[2]);
            ids.add(id);
        }

        while (ids.contains(startId)) {
            startId += 1;
        }

        serverNumber = startId;

        jedis.set("server."+serverName+"."+serverNumber, serverUUID);
        jedis.del("lock." + serverName+".key");
        JedisManager.returnJedis(jedis);

        getLogger().info("Name: "+serverName+" Number: "+serverNumber+" UUID: "+serverUUID);

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
        while (jedis.setnx("lock." + serverName+".key", System.currentTimeMillis() + 30000 + "") == 0) {
            String lock = jedis.get("lock." + serverName+".key");
            long time = Long.parseLong(lock != null ? lock : "0");
            if (System.currentTimeMillis() > time) {
                time = Long.parseLong(jedis.getSet("lock." + serverName+".key", System.currentTimeMillis() + 30000 + ""));
                if (System.currentTimeMillis() < time) {
                    continue;
                }
            } else {
                continue;
            }
            break;
        }
        jedis.del("server."+serverName+"."+serverNumber);
        jedis.del("lock." + serverName+".key");
        JedisManager.returnJedis(jedis);

        removeServer();

        JedisManager.shutDown();
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public String getServerName() {
        return serverName;
    }

    public UserLoader getUserLoader() {
        return userLoader;
    }

    public MainConfig getServerConfig() {
        return serverConfig;
    }

    private void updateServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("updateServer", serverUUID);
        netCommandSTB.addArg("IP", getServer().getIp());
        netCommandSTB.addArg("port", getServer().getPort());
        netCommandSTB.addArg("serverName", serverName);
        netCommandSTB.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void sendHeartbeat() {
        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("heartbeat", getServer().getPort(), getServer().getIp());
        netCommandSTSC.addArg("serverName", serverName);
        netCommandSTSC.addArg("serverUUID", serverUUID);
        netCommandSTSC.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTSC.flush();
    }

    public void removeServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("removeServer", serverUUID);
        netCommandSTB.flush();

        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("removeServer", getServer().getPort(), getServer().getIp());
        netCommandSTSC.addArg("serverUUID", serverUUID);
        netCommandSTSC.flush();
    }

}
