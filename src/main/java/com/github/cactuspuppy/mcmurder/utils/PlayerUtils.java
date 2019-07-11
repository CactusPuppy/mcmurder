package com.github.cactuspuppy.mcmurder.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        OfflinePlayer p;
        try {
            p = Bukkit.getPlayer(name);
        } catch (Exception e) {
            p = null;
        }
        if (p != null) {
            return p.getUniqueId();
        } else if (nameUUIDCache.containsKey(name)) {
            return nameUUIDCache.get(name);
        } else {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                String response = queryURL(url);
                JSONObject responseJSON = (JSONObject) new JSONParser().parse(response);
                String uuidString = (String) responseJSON.get("id");
                uuidString = uuidString.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5");
                UUID u = UUID.fromString(uuidString);
                nameUUIDCache.put(name, u);
                return u;
            } catch (MalformedURLException e) {
                Logger.logWarning(PlayerUtils.class, "API URL invalid!", e);
            } catch (IOException e) {
                Logger.logWarning(PlayerUtils.class, "Issue retrieving JSON payload", e);
            } catch (ParseException e) {
                Logger.logWarning(PlayerUtils.class, "Issue parsing JSON payload", e);
            }
            return null;
        }
    }

    private static String queryURL(URL url) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();
        if (responseCode != 200) {
            String response = "Bad response code while querying Mojang API.\n" +
                              "Code: " + responseCode + "\n" +
                              "Message: " + responseMessage + "\n" +
                              "Queried: " + url.toString();
            throw new RuntimeException(response);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = reader.readLine();
            while (line != null) {
                responseBuilder.append(line);
                line = reader.readLine();
            }
        }
        return responseBuilder.toString();
    }
}
