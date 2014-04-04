package com.rmb938.bukkit.base;

import com.rmb938.bukkit.base.config.MainConfig;
import com.rmb938.bukkit.base.database.UserLoader;
import com.rmb938.bukkit.base.jedis.NetCommandHandlerBTS;
import com.rmb938.bukkit.base.jedis.NetCommandHandlerSCTS;
import com.rmb938.bukkit.base.listeners.PlayerListener;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.command.server.NetCommandSTB;
import com.rmb938.jedis.net.command.server.NetCommandSTSC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.util.logging.Level;

public class MN2BukkitBase extends JavaPlugin {

    private UserLoader userLoader;
    private MainConfig serverConfig;

    @Override
    public void onEnable() {
        serverConfig = new MainConfig(this);
        try {
            serverConfig.init();
        } catch (net.cubespace.Yamler.Config.InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null, e);
            return;
        }

        DatabaseAPI.initializeMySQL(serverConfig.mySQL_userName, serverConfig.mySQL_password, serverConfig.mySQL_database, serverConfig.mySQL_address, serverConfig.mySQL_port);

        JedisManager.connectToRedis(serverConfig.redis_address);
        JedisManager.setUpDelegates();

        new NetCommandHandlerBTS(this);
        new NetCommandHandlerSCTS(this);

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

    public UserLoader getUserLoader() {
        return userLoader;
    }

    public MainConfig getServerConfig() {
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
        Jedis jedis = JedisManager.getJedis();
        String serverName = jedis.get(getServer().getIp()+"."+getServer().getPort());
        JedisManager.returnJedis(jedis);
        NetCommandSTB netCommandSTB = new NetCommandSTB("updateServer", getServer().getIp()+"."+serverName+"."+getServer().getPort());
        netCommandSTB.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTB.addArg("maxPlayers", getServer().getMaxPlayers());
        netCommandSTB.flush();
    }

    private void sendHeartbeat() {
        Jedis jedis = JedisManager.getJedis();
        String serverName = jedis.get(getServer().getIp()+"."+getServer().getPort());
        JedisManager.returnJedis(jedis);
        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("heartbeat", getServer().getPort(), getServer().getIp());
        netCommandSTSC.addArg("serverName", serverName);
        netCommandSTSC.addArg("currentPlayers", getServer().getOnlinePlayers().length);
        netCommandSTSC.flush();
    }

    public void removeServer() {
        Jedis jedis = JedisManager.getJedis();
        String serverName = jedis.get(getServer().getIp()+"."+getServer().getPort());
        JedisManager.returnJedis(jedis);
        NetCommandSTB netCommandSTB = new NetCommandSTB("removeServer", serverName+"."+getServer().getPort());
        netCommandSTB.flush();

        NetCommandSTSC netCommandSTSC = new NetCommandSTSC("removeServer", getServer().getPort(), getServer().getIp());
        netCommandSTSC.flush();
    }

}
