package com.rmb938.bukkit.base.listeners;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.base.event.PostJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final MN2BukkitBase plugin;

    public PlayerListener(MN2BukkitBase plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = plugin.getUserLoader().getUser(event.getPlayer());
        if (user == null) {
            event.getPlayer().kickPlayer("Error loading user data please report.");
            return;
        }
        PostJoinEvent postJoinEvent = new PostJoinEvent(user);
        Bukkit.getPluginManager().callEvent(postJoinEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitSave(PlayerQuitEvent event) {
        plugin.getUserLoader().saveUser(event.getPlayer(), true);
    }
}
