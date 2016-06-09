package me.jaime29010.combatactionbar.hooks;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Jaime Martinez Rincon aka MrJaime on 04/06/2016.
 */
public interface PluginHook {
    boolean hook(Plugin plugin, PluginManager manager);
    int getDuration();
}
