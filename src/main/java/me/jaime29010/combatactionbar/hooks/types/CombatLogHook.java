package me.jaime29010.combatactionbar.hooks.types;

import com.jackproehl.plugins.CombatLog;
import me.jaime29010.combatactionbar.hooks.PluginHook;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class CombatLogHook implements PluginHook {
    private CombatLog main;

    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        try {
            main = (CombatLog) plugin;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int getDuration() {
        return main.tagDuration;
    }
}
