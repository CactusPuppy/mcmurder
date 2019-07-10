package com.github.cactuspuppy.mcmurder.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class PlayerUtils implements Listener {
    private static BiHashMap<String, UUID> nameUUIDCache = new BiHashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        nameUUIDCache.put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
    }

    public static boolean validUsername(String name) {
        if (name.length() < 3 || name.length() > 16) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_'))) {
                return false;
            }
        }
        return true;
    }


    public static UUID getOnlinePlayer(String name) {
        if (!validUsername(name)) {
            return null;
        }
        Player p = Bukkit.getPlayerExact(name);
        return (p != null && p.isOnline())
                ? p.getUniqueId()
                : null;
    }

    public static UUID getOfflinePlayer(String name) {
        if (!validUsername(name)) {
            return null;
        }

    }
}
