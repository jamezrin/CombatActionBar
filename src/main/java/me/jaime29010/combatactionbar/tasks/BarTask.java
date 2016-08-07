package me.jaime29010.combatactionbar.tasks;

import me.jaime29010.combatactionbar.Main;
import me.jaime29010.combatactionbar.utils.ActionBarHelper;
import me.jaime29010.combatactionbar.utils.PluginUtils;
import me.jaime29010.combatactionbar.utils.SoundInfo;
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
        if (main.isPermitted(player)) {
            if (time > 0) {
                ActionBarHelper.sendActionBar(player, PluginUtils.colorize(main.getTagText()
                        .replace("{left}", main.getBar().substring(0, time * main.getCharacter().length()))
                        .replace("{right}", main.getBar().substring(time * main.getCharacter().length(), main.getBar().length()))
                        .replace("{time}", String.valueOf(time)))
                );

                SoundInfo.play(main.getTagSound(), player);
                time--;
            } else {
                ActionBarHelper.sendActionBar(player, PluginUtils.colorize(main.getUntagText()));

                SoundInfo.play(main.getUntagSound(), player);
                main.cancelTask(player);
            }
        } else main.cancelTask(player);
    }
}
