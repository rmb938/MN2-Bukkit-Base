package com.rmb938.bukkit.base.database;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.base.entity.info.UserInfo;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UserLoader {

    private final MN2BukkitBase plugin;

    private HashMap<UUID, User> users = new HashMap<>();

    public UserLoader(MN2BukkitBase plugin) {
        this.plugin = plugin;
        createTable();
    }

    public void createTable() {
        if (DatabaseAPI.getMySQLDatabase().isTable("mn2_users") == false) {
            DatabaseAPI.getMySQLDatabase().createTable("CREATE TABLE IF NOT EXISTS `mn2_users` (" +
                    " `userUUID` varchar(36) NOT NULL," +
                    " `lastUserName` varchar(16) NOT NULL," +
                    " `server` varchar(64) NOT NULL," +
                    " PRIMARY KEY (`userUUID`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
    }

    public User getUser(Player player) {
        if (users.containsKey(player.getUniqueId())) {
            return users.get(player.getUniqueId());
        }
        ArrayList beans = DatabaseAPI.getMySQLDatabase().getBeansInfo("select userUUID, lastUserName from `mn2_users` where userUUID='"+player.getUniqueId().toString()+"'", new BeanListHandler<>(User.class));
        if (beans.isEmpty()) {
            plugin.getLogger().info("No user found for "+player.getName()+" ("+player.getUniqueId().toString()+") creating new user.");
            createUser(player);
            return getUser(player);
        }
        if (beans.size() > 1) {
            plugin.getLogger().severe("Multiple users found for "+player.getName()+" ("+player.getUniqueId().toString()+")");
            return null;
        }
        User user = (User) beans.get(0);

        for (UserInfoLoader userInfoLoader : UserInfoLoader.getUserInfoLoaders().values()) {
            if (userInfoLoader.loadUserInfo(user) == null) {
                userInfoLoader.createUserInfo(user);
                userInfoLoader.loadUserInfo(user);
            }
        }

        users.put(player.getUniqueId(), user);
        return (User) beans.get(0);
    }

    private void createUser(Player player) {
        Jedis jedis = JedisManager.getJedis();
        JedisManager.returnJedis(jedis);
        DatabaseAPI.getMySQLDatabase().updateQueryPS("INSERT INTO `mn2_users` (uuid, lastUserName, server) values (?, ?, ?)", player.getUniqueId(), player.getName(), plugin.getServerName());
    }

    public void saveUser(Player player) {
        User user = getUser(player);
        if (user == null) {
            return;
        }
        DatabaseAPI.getMySQLDatabase().updateQueryPS("UPDATE `mn2_users` SET lastUserName = ? where userUUID='"+user.getUserUUID()+"'", player.getName());
        for (UserInfo userInfo : user.getUserInfo()) {
            UserInfoLoader.getUserInfoLoaders().get(userInfo.getUserInfoName()).saveUserInfo(user);
        }
        plugin.getLogger().info("Saved User " + player.getName()+" ("+user.getUserUUID()+")");
    }

    public void removeUser(Player player) {
        users.remove(player.getUniqueId());
    }

}
