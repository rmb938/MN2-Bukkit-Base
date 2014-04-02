package com.rmb938.bukkit.base.entity.info;

public class UserInfo {

    private final String userInfoName;

    private UserInfo(String userInfoName) {
        this.userInfoName = userInfoName;
    }

    public String getUserInfoName() {
        return userInfoName;
    }
}
