package me.jaimemartz.combatactionbar.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ConfigUtil {
    public static FileConfiguration loadConfig(String name, JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), name);
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        if (!file.exists()) {
            try {
                Files.copy(plugin.getResource(file.getName()), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static boolean saveConfig(FileConfiguration config, String name, JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), name);
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
}
