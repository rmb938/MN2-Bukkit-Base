package com.rmb938.bukkit.base.entity;

import com.rmb938.bukkit.base.entity.info.UserInfo;

import java.util.ArrayList;

public class User {

    private String userUUID;
    private String lastUserName;
    private ArrayList<UserInfo> userInfo = new ArrayList<>();

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

    public ArrayList<UserInfo> getUserInfo() {
        return userInfo;
    }
}
