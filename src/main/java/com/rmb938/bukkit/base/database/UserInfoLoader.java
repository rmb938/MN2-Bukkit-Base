package com.rmb938.bukkit.base.database;

import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.base.entity.info.UserInfo;

import java.util.HashMap;

public abstract class UserInfoLoader<T extends UserInfo> {

    private static HashMap<String, UserInfoLoader> userInfoLoaders = new HashMap<>();

    public static HashMap<String, UserInfoLoader> getUserInfoLoaders() {
        return userInfoLoaders;
    }

    public UserInfoLoader(String userInfoName) {
        createTable();
        UserInfoLoader.getUserInfoLoaders().put(userInfoName, this);
    }

    public abstract void createTable();

    public abstract T loadUserInfo(User user);

    public abstract void createUserInfo(User user);

    public abstract void saveUserInfo(User user);

}
