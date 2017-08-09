package me.jaimemartz.combatactionbar;

import me.jaimemartz.combatactionbar.utils.PluginUtils;
import me.jaimemartz.combatactionbar.utils.SoundInfo;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class BarTask extends BukkitRunnable {
    private final CombatActionBar plugin;
    private final Player player;
    private int time;

    public BarTask(CombatActionBar plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.time = plugin.getDuration();
        runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        if (plugin.isTagable(player)) {
            if (time > 0) {
                plugin.sendBar(player, PluginUtils.colorize(plugin.getTagText()
                        .replace("{left}", plugin.getBar().substring(0, time * plugin.getCharacter().length()))
                        .replace("{right}", plugin.getBar().substring(time * plugin.getCharacter().length(), plugin.getBar().length()))
                        .replace("{time}", String.valueOf(time))));
                SoundInfo.play(plugin.getTagSound(), player);
                time--;
            } else {
                plugin.sendBar(player, PluginUtils.colorize(plugin.getUntagText()));
                SoundInfo.play(plugin.getUntagSound(), player);
                plugin.cancelTask(player);
            }
        } else plugin.cancelTask(player);
    }
}
