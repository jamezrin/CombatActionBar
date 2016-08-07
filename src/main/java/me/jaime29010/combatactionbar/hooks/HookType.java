package me.jaime29010.combatactionbar.hooks;

import me.jaime29010.combatactionbar.hooks.types.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public enum HookType {
    COMBAT_LOG ("CombatLog", CombatLogHook.class),
    COMBAT_TAG("CombatTag", CombatTagHook.class),
    COMBAT_TAG_PLUS("CombatTagPlus", CombatTagPlusHook.class),
    PVP_MANAGER_FREE("PvPManager", PvPManagerHook.class),
    ANTI_COMBAT_LOG("AntiCombatLog", AntiCombatLogHook.class),
    GRIEF_PREVENTION("GriefPrevention", GriefPreventionHook.class);

    private final String name;
    private final Class<? extends PluginHook> hook;
    private final Plugin plugin;
    HookType(String name, Class<? extends PluginHook> hook) {
        this.name = name;
        this.hook = hook;
        plugin = Bukkit.getPluginManager().getPlugin(name);
    }

    public String getPluginName() {
        return name;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Class<? extends PluginHook> getHook() {
        return hook;
    }

    public boolean check() {
        return plugin != null && plugin.isEnabled();
    }
}
