package me.jaime29010.combatactionbar.hooks.types;

import com.jackproehl.plugins.CombatLog;
import me.jaime29010.combatactionbar.hooks.PluginHook;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Jaime Martinez Rincon aka MrJaime on 09/06/2016.
 */
public class CombatLogHook implements PluginHook {
    private CombatLog main;
    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        main = (CombatLog) plugin;
        return true;
    }

    @Override
    public int getDuration() {
        return main.tagDuration;
    }
}
