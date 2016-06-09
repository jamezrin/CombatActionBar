package me.jaime29010.combatactionbar.hooks;

import me.jaime29010.combatactionbar.hooks.types.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Created by Jaime Martinez Rincon aka MrJaime on 04/06/2016.
 */
public enum HookType {
    COMBAT_LOG ("CombatLog", CombatLogHook.class),
    COMBAT_TAG("CombatTag", CombatTagHook.class),
    COMBAT_TAG_PLUS("CombatTagPlus", CombatTagPlusHook.class),
    PVP_MANAGER_FREE("PvPManager", PvPManagerHook.class),
    ANTI_COMBAT_LOG("AntiCombatLog", AntiCombatLogHook.class),
    GRIEF_PREVENTION("GriefPrevention", GriefPreventionHook.class);

    private final String name;
    private final Class<? extends PluginHook> hook;
    HookType(String name, Class<? extends PluginHook> hook) {
        this.name = name;
        this.hook = hook;
    }

    public String getPluginName() {
        return name;
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    public Class<? extends PluginHook> getHook() {
        return hook;
    }

    public boolean check() {
        Plugin plugin = getPlugin();
        return plugin != null && plugin.isEnabled();
    }
}
