package com.rmb938.bukkit.base.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.base.entity.info.UserInfo;
import com.rmb938.database.DatabaseAPI;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class UserLoader {

    private final MN2BukkitBase plugin;

    private HashMap<UUID, User> users = new HashMap<>();

    public UserLoader(MN2BukkitBase plugin) {
        this.plugin = plugin;
        createTable();
    }

    private void createTable() {
        if (DatabaseAPI.getMongoDatabase().collectionExists("mn2_users") == false) {
            DatabaseAPI.getMongoDatabase().createCollection("mn2_users");
        }
    }

    public User getUser(Player player) {
        if (users.containsKey(player.getUniqueId())) {
            return users.get(player.getUniqueId());
        }
        DBObject userObject = DatabaseAPI.getMongoDatabase().findOne("mn2_users", new BasicDBObject("userUUID", player.getUniqueId().toString()));
        if (userObject == null) {
            plugin.getLogger().info("No user found for "+player.getName()+" ("+player.getUniqueId().toString()+") creating new user.");
            player.kickPlayer("Unknown MN2 Profile");
            return null;
        }

        User user = new User();
        user.setLastUserName((String) userObject.get("lastUserName"));
        user.setUserUUID((String) userObject.get("userUUID"));

        for (UserInfoLoader userInfoLoader : UserInfoLoader.getUserInfoLoaders().values()) {
            if (userInfoLoader.loadUserInfo(user, player) == null) {
                userInfoLoader.createUserInfo(user, player);
                userInfoLoader.loadUserInfo(user, player);
            }
        }

        users.put(player.getUniqueId(), user);
        return user;
    }

    public void saveUser(Player player, boolean remove) {
        User user = getUser(player);
        if (user == null) {
            return;
        }
        for (UserInfo userInfo : user.getUserInfo().values()) {
            UserInfoLoader userInfoLoader = UserInfoLoader.getUserInfoLoaders().get(userInfo.getClass());
            userInfoLoader.saveUserInfo(user, player, remove);
        }
        DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", user.getUserUUID()), new BasicDBObject("$set", new BasicDBObject("lastUserName", player.getName())));
        plugin.getLogger().info("Saved User " + player.getName() + " (" + user.getUserUUID() + ")");
        if (remove == true) {
            users.remove(player.getUniqueId());
        }
    }

}
