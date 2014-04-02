package com.rmb938.bukkit.base.jedis;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

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
            String fromBungee = jsonObject.getString("fromBungee");
            String toServer = jsonObject.getString("toServerName");
            String[] serverInfo = toServer.split(".");

            if (serverInfo[1].equalsIgnoreCase(plugin.getServerConfig().serverName)) {
                return;
            }

            if (serverInfo[1].equalsIgnoreCase("*") == false) {
                if (Integer.parseInt(serverInfo[1]) != plugin.getServer().getPort()) {
                    return;
                }
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "shutdown":
                    plugin.removeServer();
                    plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            plugin.getServer().shutdown();
                        }
                    }, 200L);
                    break;
                default:
                    plugin.getLogger().info("Unknown BTS Command MN2BukkitBase "+command);
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }

}
