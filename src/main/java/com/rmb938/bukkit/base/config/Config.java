package com.rmb938.bukkit.base.config;

import org.bukkit.plugin.Plugin;

import java.io.File;

public class Config extends ConfigModel {

	public Config(Plugin plugin) {
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
		CONFIG_HEADER = "MN2 Bukkit Base Configuration File";
	}

	public String redis_address = "127.0.0.1";

    public String mySQL_address = "127.0.0.1";
    public int mySQL_port = 3306;
    public String mySQL_userName = "userName";
    public String mySQL_password = "password";
    public String mySQL_database = "database";

}
