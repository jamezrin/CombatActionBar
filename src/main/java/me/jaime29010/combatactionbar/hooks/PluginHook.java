package me.jaime29010.combatactionbar.hooks;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public interface PluginHook {
    boolean hook(Plugin plugin, PluginManager manager);
    int getDuration();
}
