package me.jaime29010.combatactionbar.hooks.types;

import me.jaime29010.combatactionbar.hooks.PluginHook;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Settings;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class CombatTagPlusHook implements PluginHook {
    private Settings settings;

    @Override
    public boolean hook(Plugin plugin, PluginManager manager) {
        try {
            CombatTagPlus main = (CombatTagPlus) plugin;
            settings = main.getSettings();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int getDuration() {
        return settings.getTagDuration();
    }
}
