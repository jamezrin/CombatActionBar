package me.jaime29010.combatactionbar.hooks.types;

import me.NoChance.PvPManager.Config.Settings;
import me.jaime29010.combatactionbar.hooks.PluginHook;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class PvPManagerHook implements PluginHook {

    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        return true;
    }

    @Override
    public int getDuration() {
        return Settings.getTimeInCombat();
    }
}
