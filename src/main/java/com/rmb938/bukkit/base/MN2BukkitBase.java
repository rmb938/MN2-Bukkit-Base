package com.rmb938.bukkit.base;

import com.rmb938.bukkit.base.config.Config;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.command.server.NetCommandSTB;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MN2BukkitBase extends JavaPlugin {

    private String serverName = null;
    private Config config;

    public void onEnable() {
        config = new Config(this);
        try {
            config.init();
        } catch (InvalidConfigurationException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        JedisManager.connectToRedis(config.redis_address);
        JedisManager.setUpDelegates();
        addServer();
        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                updateServer();
            }
        }, 200L, 200L);
    }

    public void onDisable() {
        JedisManager.shutDown();
    }

    private void updateServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("updateServer", serverName);
        netCommandSTB.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void addServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("addServer", serverName);
        netCommandSTB.addArg("currentPlayers", 0);
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void removeServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("removeServer", serverName);
        netCommandSTB.flush();
    }

}
