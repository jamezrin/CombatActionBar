package me.jaime29010.combatactionbar.hooks.types;

import me.jaime29010.combatactionbar.hooks.PluginHook;
import net.techcable.combattag.CombatTag;
import net.techcable.combattag.config.CombatTagConfig;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Jaime Martinez Rincon aka MrJaime on 09/06/2016.
 */
public class CombatTagHook implements PluginHook {
    private CombatTagConfig settings;
    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        CombatTag main = (CombatTag) plugin;
        settings = main.getSettings();
        return true;
    }

    @Override
    public int getDuration() {
        return settings.getTagDuration();
    }
}
