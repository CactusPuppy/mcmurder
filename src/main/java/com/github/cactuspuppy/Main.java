package com.github.cactuspuppy;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.cactuspuppy.utils.Config;
import com.github.cactuspuppy.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Main extends JavaPlugin {
    @Getter
    private static Main instance;
    @Getter
    private Config mainConfig;

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
            ChatColor.LIGHT_PURPLE + "Time Elapsed: " + ChatColor.GOLD + "%1$.2fms (%2$dμs)",
            elapsedNanos / 10e6, elapsedNanos / 10e3));
    }

    private void abortSetup() {
        this.getLogger().severe(ChatColor.RED + "MCMurder failed to initialize");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private boolean baseSetup() {
        Logger.setOutput(java.util.logging.Logger.getLogger(ChatColor.RED + "MCMurder" + ChatColor.RESET));
        return createConfig();
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
}
