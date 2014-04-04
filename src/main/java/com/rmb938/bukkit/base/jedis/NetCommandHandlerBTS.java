package com.rmb938.bukkit.base.jedis;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.logging.Level;

public class NetCommandHandlerBTS extends NetCommandHandler {

    private final MN2BukkitBase plugin;

    public NetCommandHandlerBTS(MN2BukkitBase plugin) {
        NetCommandHandler.addHandler(NetChannel.BUNGEE_TO_SERVER, this);
        this.plugin = plugin;
    }

    @Override
    public void handle(JSONObject jsonObject) {
        try {
            String fromBungee = jsonObject.getString("from");
            String toServer = jsonObject.getString("to");

            Jedis jedis = JedisManager.getJedis();
            String serverName = jedis.get(plugin.getServer().getIp() + "." + plugin.getServer().getPort());
            JedisManager.returnJedis(jedis);

            String testIPNamePort = plugin.getServer().getIp() + "." + serverName + "." + plugin.getServer().getPort();
            String testIPName = plugin.getServer().getIp() + "." + serverName + ".*";

            if (toServer.equalsIgnoreCase(testIPNamePort) == false && toServer.equalsIgnoreCase(testIPName) == false) {
                return;
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "shutdown":
                    plugin.getServer().getScheduler().cancelTasks(plugin);
                    plugin.getServer().shutdown();
                    break;
                default:
                    plugin.getLogger().info("Unknown BTS Command MN2BukkitBase " + command);
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }

}
