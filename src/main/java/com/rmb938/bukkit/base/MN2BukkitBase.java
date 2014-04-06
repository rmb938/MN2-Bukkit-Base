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
    private MainConfig serverConfig;
    private String serverName;
    private String serverUUID;

    @Override
    public void onEnable() {
        serverConfig = new MainConfig(this);
        try {
            serverConfig.init();
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

        try {
            JedisManager.returnJedis(JedisManager.getJedis());
        } catch (Exception e) {
            getLogger().warning("Unable to connect to redis. Closing");
            getServer().shutdown();
            return;
        }

        new NetCommandHandlerBTS(this);
        new NetCommandHandlerSCTS(this);

        Jedis jedis = JedisManager.getJedis();
        serverName = jedis.get(getServer().getIp()+"."+getServer().getPort());
        serverUUID = jedis.get(getServer().getIp()+"."+getServer().getPort()+".uuid");
        jedis.del(getServer().getIp()+"."+getServer().getPort());
        jedis.del(getServer().getIp()+"."+getServer().getPort()+".uuid");
        JedisManager.returnJedis(jedis);

        getLogger().info("Name: "+serverName+" UUID: "+serverUUID);

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
        jedis.del(getServer().getIp()+"."+getServer().getPort());
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
