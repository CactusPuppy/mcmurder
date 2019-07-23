package com.github.cactuspuppy.mcmurder.game.murder.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerMurderEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private Player player;

    @SuppressWarnings("WeakerAccess")
    public PlayerMurderEvent(Player p) {
        player = p;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
