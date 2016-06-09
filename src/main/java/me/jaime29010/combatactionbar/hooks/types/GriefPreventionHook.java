package me.jaime29010.combatactionbar.hooks.types;

import me.jaime29010.combatactionbar.hooks.PluginHook;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Jaime Martinez Rincon aka MrJaime on 04/06/2016.
 */
public final class GriefPreventionHook implements PluginHook {
    private GriefPrevention main;
    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        main = (GriefPrevention) plugin;
        return true;
    }

    @Override
    public int getDuration() {
        return main.config_pvp_combatTimeoutSeconds;
    }
}
