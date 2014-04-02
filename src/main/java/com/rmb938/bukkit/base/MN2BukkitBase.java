package com.rmb938.bukkit.base;

import com.rmb938.bukkit.base.config.Config;
import com.rmb938.bukkit.base.database.UserLoader;
import com.rmb938.bukkit.base.listeners.PlayerListener;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.command.server.NetCommandSTB;
import com.rmb938.jedis.net.command.server.NetCommandSTSC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MN2BukkitBase extends JavaPlugin {

    private UserLoader userLoader;
    private Config serverConfig;

    @Override
    public void onEnable() {
        serverConfig = new Config(this);
        try {
            serverConfig.init();
        } catch (InvalidConfigurationException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        DatabaseAPI.initializeMySQL(serverConfig.mySQL_userName, serverConfig.mySQL_password, serverConfig.mySQL_database, serverConfig.mySQL_address, serverConfig.mySQL_port);

        JedisManager.connectToRedis(serverConfig.redis_address);
        JedisManager.setUpDelegates();

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
        JedisManager.shutDown();
    }

    public UserLoader getUserLoader() {
        return userLoader;
    }

    public Config getServerConfig() {
        return serverConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stop")) {
            return false;
        }
        return super.onCommand(sender, command, label, args);
    }

    private void updateServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("updateServer", serverConfig.serverName+"."+getServer().getPort());
        netCommandSTB.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void sendHeartbeat() {
        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("heartbeat", getServer().getIp(), getServer().getPort());
        netCommandSTSC.flush();
    }

    public void removeServer() {
        NetCommandSTB netCommandSTB = new NetCommandSTB("removeServer", serverConfig.serverName+"."+getServer().getPort());
        netCommandSTB.flush();
    }

}
