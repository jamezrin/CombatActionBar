package me.jaime29010.combatactionbar.utils;

import org.bukkit.ChatColor;

public final class PluginUtils {
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
