package com.github.cactuspuppy.mcmurder.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
public class FileConfig extends Config {
    @Getter
    private File configFile;

    public void save() {
        try {
            save(configFile);
        } catch (IOException e) {
            //FIXME
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            //FIXME
            e.printStackTrace();
        }
    }

    public void set(String key, String value) {
        put(key, value);
    }
}
