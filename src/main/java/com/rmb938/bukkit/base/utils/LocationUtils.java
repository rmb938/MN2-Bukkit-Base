package com.rmb938.bukkit.base.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    public static String locationToString(Location location, boolean withDirection) {
        String loc;
        loc = location.getWorld().getName()+","+location.getX()+","+location.getY()+","+location.getZ();
        if (withDirection == true) {
            loc += ","+location.getPitch()+","+location.getYaw();
        }
        return loc;
    }

    public static Location stringToLocation(String loc) {
        Location location;
        String[] locInfo = loc.split(",");
        location = new Location(Bukkit.getWorld(locInfo[0]), Double.parseDouble(locInfo[1]),
                Double.parseDouble(locInfo[2]), Double.parseDouble(locInfo[3]));
        if (locInfo.length == 6) {
            location.setPitch(Float.parseFloat(locInfo[4]));
            location.setYaw(Float.parseFloat(locInfo[5]));
        }
        return location;
    }

}
