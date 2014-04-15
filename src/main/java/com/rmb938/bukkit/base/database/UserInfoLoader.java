package com.rmb938.bukkit.base.database;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.base.entity.info.UserInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public abstract class UserInfoLoader<T extends UserInfo> {

    private static HashMap<Class<? extends UserInfo>, UserInfoLoader> userInfoLoaders = new HashMap<>();

    public static HashMap<Class<? extends UserInfo>, UserInfoLoader> getUserInfoLoaders() {
        return userInfoLoaders;
    }

    private MN2BukkitBase plugin;

    public UserInfoLoader(Class<? extends UserInfo> userInfo) {
        plugin = (MN2BukkitBase) Bukkit.getPluginManager().getPlugin("MN2BukkitBase");
            createTable();
        UserInfoLoader.getUserInfoLoaders().put(userInfo, this);
    }

    public MN2BukkitBase getPlugin() {
        return plugin;
    }

    public abstract void createTable();

    public abstract T loadUserInfo(User user, Player player);

    public abstract void createUserInfo(User user, Player player);

    public abstract void saveUserInfo(User user, Player player, boolean remove);

}
