package com.rmb938.bukkit.base.event;

import com.rmb938.bukkit.base.entity.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PostJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final User user;

    public PostJoinEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
