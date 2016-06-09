package me.jaime29010.combatactionbar.hooks.types;

import me.jaime29010.combatactionbar.hooks.PluginHook;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Jaime Martinez Rincon aka MrJaime on 04/06/2016.
 */
public final class AntiCombatLogHook implements PluginHook {
    private Configuration config;
    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        config = plugin.getConfig();
        return true;
    }

    @Override
    public int getDuration() {
        return config.getInt("combattime");
    }
}
