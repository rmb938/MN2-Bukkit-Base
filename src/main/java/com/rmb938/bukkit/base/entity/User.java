package com.rmb938.bukkit.base.entity;

import com.rmb938.bukkit.base.entity.info.UserInfo;

import java.util.HashMap;

public class User {

    private String userUUID;
    private String lastUserName;
    private HashMap<Class<? extends UserInfo>, UserInfo> userInfo = new HashMap<>();

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public HashMap<Class<? extends UserInfo>, UserInfo> getUserInfo() {
        return userInfo;
    }
}
