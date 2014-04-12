package com.rmb938.bukkit.base.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class MainConfig extends Config {

	public MainConfig(Plugin plugin) {
        CONFIG_HEADER = new String[]{"MN2 Bukkit Base Configuration File"};
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
	}

    @Comment("The IP address for the redis server")
	public String redis_address = "127.0.0.1";

    @Comment("The IP address from the mongo server")
    public String mongo_address = "127.0.0.1";
    @Comment("The port for the mongo server")
    public int mongo_port = 27017;
    @Comment("The database name for the mongo server")
    public String mongo_database = "minecraft";

}
