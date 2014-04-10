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
            String fromBungee = jsonObject.getString("from");
            String toServer = jsonObject.getString("to");

            if (toServer.equalsIgnoreCase("*") == false) {
                if (toServer.equalsIgnoreCase(plugin.getServerUUID()) == false) {
                    return;
                }
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "shutdown":
                    plugin.getServer().getScheduler().cancelTasks(plugin);
                    plugin.getServer().shutdown();
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }

}
