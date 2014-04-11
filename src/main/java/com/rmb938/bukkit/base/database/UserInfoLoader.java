package com.rmb938.bukkit.base.database;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.base.entity.info.UserInfo;
import org.bukkit.Bukkit;

import java.util.HashMap;

public abstract class UserInfoLoader<T extends UserInfo> {

    private static HashMap<Class<? extends UserInfo>, UserInfoLoader> userInfoLoaders = new HashMap<>();

    public static HashMap<Class<? extends UserInfo>, UserInfoLoader> getUserInfoLoaders() {
        return userInfoLoaders;
    }

    public UserInfoLoader(Class<? extends UserInfo> userInfo) {
        MN2BukkitBase plugin = (MN2BukkitBase) Bukkit.getPluginManager().getPlugin("MN2BukkitBase");
        if (plugin.getServerConfig().users_save == false) {
            createTable();
        }
        UserInfoLoader.getUserInfoLoaders().put(userInfo, this);
    }

    public abstract void createTable();

    public abstract T loadUserInfo(User user);

    public abstract void createUserInfo(User user);

    public abstract void saveUserInfo(User user);

    public abstract void createTempUserInfo(User user);

}
