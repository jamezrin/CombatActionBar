package me.jaimemartz.combatactionbar;

import me.jaimemartz.combatactionbar.utils.ConfigUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public enum HookType {
    COMBAT_LOG("CombatLog", "https://dev.bukkit.org/bukkit-plugins/combatlog/", "combatlog.yml", "Tag-Duration"),
    COMBAT_TAG("CombatTag", "https://www.spigotmc.org/resources/3182/", "config.yml", "Tag-Duration"),
    COMBAT_TAG_PLUS("CombatTagPlus", "https://www.spigotmc.org/resources/4775/", "config.yml", "tag-duration"),
    PVP_MANAGER("PvPManager", "https://www.spigotmc.org/resources/845/", "config.yml", "Tagged In Combat.Time"),
    ANTI_COMBAT_LOG("AntiCombatLog", "https://www.spigotmc.org/resources/4278/", "config.yml", "combattime"),
    GRIEF_PREVENTION("GriefPrevention", "https://www.spigotmc.org/resources/1884/", "config.yml", "PvP.CombatTimeoutSeconds");

    private final String name;
    private final String url;
    private final String file;
    private final String path;
    private final Plugin plugin;
    HookType(String name, String url, String file, String path) {
        this.name = name;
        this.url = url;
        this.file = file;
        this.path = path;
        plugin = Bukkit.getPluginManager().getPlugin(name);
    }

    public String getPluginName() {
        return name;
    }

    public String getPageURL() {
        return url;
    }

    public String getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public int hook() {
        Validate.isTrue(check(), "Tried to hook to an not loaded plugin");
        try {
            FileConfiguration config = ConfigUtil.loadConfig(file, (JavaPlugin) plugin);
            return config != null ? config.getInt(path) : -1;
        } catch (Exception e) {
            new Exception("Failed to hook into " + name, e).printStackTrace();
        }
        return -1;
    }

    public boolean check() {
        return plugin != null && plugin.isEnabled();
    }
}
