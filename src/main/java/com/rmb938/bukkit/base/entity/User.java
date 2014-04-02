package com.rmb938.bukkit.base.entity;

import com.google.common.base.Preconditions;
import com.rmb938.bukkit.base.entity.info.UserInfo;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class User {

    private UUID userUUID;
    private ArrayList<UserInfo> userInfos;

    public static User createUser(Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        User user = new User();
        user.setUserUUID(player.getUniqueId());
        user.setUserInfos(new ArrayList<UserInfo>());
        return user;
    }

    public UUID getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public ArrayList<UserInfo> getUserInfos() {
        return userInfos;
    }

    public void setUserInfos(ArrayList<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }
}
