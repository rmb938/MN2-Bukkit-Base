package com.rmb938.bukkit.base.entity;

import com.rmb938.bukkit.base.entity.info.UserInfo;

import java.util.ArrayList;

public class User {

    private String userUUID;
    private ArrayList<UserInfo> userInfo = new ArrayList<>();

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public ArrayList<UserInfo> getUserInfo() {
        return userInfo;
    }
}
