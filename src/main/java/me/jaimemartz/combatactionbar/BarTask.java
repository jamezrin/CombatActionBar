package me.jaimemartz.combatactionbar;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import me.jaimemartz.combatactionbar.utils.PluginUtils;
import me.jaimemartz.combatactionbar.utils.SoundInfo;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class BarTask extends BukkitRunnable {
    private final Main main;
    private final Player player;
    private int time;

    public BarTask(Main main, Player player) {
        this.main = main;
        this.player = player;
        this.time = main.getDuration();
        runTaskTimer(main, 0, 20);
    }

    @Override
    public void run() {
        if (main.isTagable(player)) {
            if (time > 0) {
                ActionBarAPI.sendActionBar(player, PluginUtils.colorize(main.getTagText()
                        .replace("{left}", main.getBar().substring(0, time * main.getCharacter().length()))
                        .replace("{right}", main.getBar().substring(time * main.getCharacter().length(), main.getBar().length()))
                        .replace("{time}", String.valueOf(time)))
                );

                SoundInfo.play(main.getTagSound(), player);
                time--;
            } else {
                ActionBarAPI.sendActionBar(player, PluginUtils.colorize(main.getUntagText()));
                SoundInfo.play(main.getUntagSound(), player);
                main.cancelTask(player);
            }
        } else main.cancelTask(player);
    }
}
