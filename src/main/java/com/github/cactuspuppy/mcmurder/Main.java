package com.github.cactuspuppy.mcmurder;

import com.github.cactuspuppy.mcmurder.game.Game;
import com.github.cactuspuppy.mcmurder.game.murder.MansionMurder;
import com.github.cactuspuppy.mcmurder.utils.PlayerUtils;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.cactuspuppy.mcmurder.utils.Config;
import com.github.cactuspuppy.mcmurder.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class Main extends JavaPlugin {
    @Getter
    private static Main instance;
    @Getter
    private Config mainConfig;
    @Getter
    private Game currentGame;

    @Override
    public void onEnable() {
        long start = System.nanoTime();
        instance = this;

        boolean baseSuccess = baseSetup();
        if (!baseSuccess) {
            abortSetup();
            return;
        }

        mainConfig = new Config();
        try {
            mainConfig.load(new File(getDataFolder(), "config.yml"));
        } catch (InvalidConfigurationException | IOException e) {
            Logger.logSevere(this.getClass(), "Could not load config.yml", e);
            abortSetup();
            return;
        }

        long elapsedNanos = System.nanoTime() - start;
        Logger.logInfo(this.getClass(), ChatColor.GREEN + "Startup complete");
        Logger.logInfo(this.getClass(), String.format(
            ChatColor.LIGHT_PURPLE + "Time Elapsed: " + ChatColor.GOLD + "%1$.2fms (%2$fus)",
            elapsedNanos / 10e6, elapsedNanos / 10e3));
    }

    private void abortSetup() {
        this.getLogger().severe(ChatColor.RED + "MCMurder failed to initialize");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private boolean baseSetup() {
        Logger.setOutput(java.util.logging.Logger.getLogger(ChatColor.RED + "MCMurder" + ChatColor.RESET));
        Bukkit.getPluginManager().registerEvents(new PlayerUtils(), this);
        return createConfig()
            && loadNewGame(MansionMurder.class);
    }

    private boolean createConfig() {
        //Get/create main config
        File dataFolder = Main.getInstance().getDataFolder();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            Logger.logSevere(this.getClass(), "Could not find or create data folder.");
            return false;
        }
        File config = new File(Main.getInstance().getDataFolder(), "config.yml");
        //Create config if not exist
        if (!config.isFile()) {
            InputStream inputStream = getResource("config.yml");
            if (inputStream == null) {
                Logger.logSevere(this.getClass(), "No packaged config.yml?!");
                return false;
            }
            try {
                FileUtils.copyToFile(inputStream, config);
            } catch (IOException e) {
                Logger.logSevere(this.getClass(), "Error while creating new config", e);
                return false;
            }
        }
        return true;
    }

    /**
     * If the current game is not locked (i.e. is swappable),<br>
     *     unloads the previous game, sets the current game, and loads the game.
     * @param game The new game to load
     * @return Whether the new game was successfully set
     */
    public boolean loadNewGame(Class<? extends Game> game) {
        if (currentGame != null && currentGame.isLocked()) {
            Logger.logSevere(this.getClass(), "Current game is locked! Please disable the current game or wait for it to finish.");
            return false;
        }
        if (currentGame != null) {
            currentGame.onUnload();
        }
        try {
            currentGame = game.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Logger.logSevere(this.getClass(), "Exception while loading game of type " + game.getName(), e);
            return false;
        }
        currentGame.onLoad();
        return true;
    }
}
