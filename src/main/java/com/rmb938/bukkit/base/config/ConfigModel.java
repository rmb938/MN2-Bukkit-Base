package com.rmb938.bukkit.base.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

    /*
    * SuperEasyConfig - Config
    *
    * Based off of codename_Bs EasyConfig v2.1
    * which was inspired by md_5
    *
    * An even awesomer super-duper-lazy Config lib!
    *
    * This program is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    *
    * @author MrFigg
    * @version 1.2
    */

public abstract class ConfigModel extends ConfigObject {
	protected transient File CONFIG_FILE = null;
	protected transient String CONFIG_HEADER = null;

	public ConfigModel() {
		CONFIG_HEADER = null;
	}

	public ConfigModel load(File file) throws InvalidConfigurationException {
		if(file==null) throw new InvalidConfigurationException(new NullPointerException());
		if(!file.exists()) throw new InvalidConfigurationException(new IOException("File doesn't exist"));
		CONFIG_FILE = file;
		return reload();
	}

	public ConfigModel reload() throws InvalidConfigurationException {
		if(CONFIG_FILE==null) throw new InvalidConfigurationException(new NullPointerException());
		if(!CONFIG_FILE.exists()) throw new InvalidConfigurationException(new IOException("File doesn't exist"));
		YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(CONFIG_FILE);
		try {
			onLoad(yamlConfig);
			yamlConfig.save(CONFIG_FILE);
		} catch(Exception ex) {
			throw new InvalidConfigurationException(ex);
		}
		return this;
	}

	public ConfigModel save(File file) throws InvalidConfigurationException {
		if(file==null) throw new InvalidConfigurationException(new NullPointerException());
		CONFIG_FILE = file;
		return save();
	}

	public ConfigModel save() throws InvalidConfigurationException {
		if(CONFIG_FILE==null) throw new InvalidConfigurationException(new NullPointerException());
		if(!CONFIG_FILE.exists()) {
			try {
				if(CONFIG_FILE.getParentFile() != null) CONFIG_FILE.getParentFile().mkdirs();
				CONFIG_FILE.createNewFile();
				if(CONFIG_HEADER!=null) {
					Writer newConfig = new BufferedWriter(new FileWriter(CONFIG_FILE));
					for(String line : CONFIG_HEADER.split("\n")) {
						newConfig.write("# "+line+"\n");
					}
					newConfig.close();
				}
			} catch(Exception ex) {
				throw new InvalidConfigurationException(ex);
			}
		}
		YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(CONFIG_FILE);
		try {
			onSave(yamlConfig);
			yamlConfig.save(CONFIG_FILE);
		} catch(Exception ex) {
			throw new InvalidConfigurationException(ex);
		}
		return this;
	}

	public ConfigModel init(File file) throws InvalidConfigurationException {
		if(file==null) throw new InvalidConfigurationException(new NullPointerException());
		CONFIG_FILE = file;
		return init();
	}

	public ConfigModel init() throws InvalidConfigurationException {
		if(CONFIG_FILE==null) throw new InvalidConfigurationException(new NullPointerException());
		if(CONFIG_FILE.exists()) return reload();
		else return save();
	}
}