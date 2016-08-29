package me.jaime29010.combatactionbar.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ConfigurationManager {
    public static FileConfiguration loadConfig(File file, Plugin plugin) {
        FileConfiguration config;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        if (!file.exists()) {
            try {
                Files.copy(plugin.getResource(file.getName()), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        return config;
    }
    public static FileConfiguration loadConfig(String name, Plugin plugin) {
        return loadConfig(new File(plugin.getDataFolder(), name), plugin);
    }

    public static boolean saveConfig(FileConfiguration config, File file, Plugin plugin) {
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean saveConfig(FileConfiguration config, String name, Plugin plugin) {
        return saveConfig(config, new File(plugin.getDataFolder(), name), plugin);
    }
}