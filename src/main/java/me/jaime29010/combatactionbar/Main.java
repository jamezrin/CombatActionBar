package me.jaime29010.combatactionbar;

import me.jaime29010.combatactionbar.hooks.HookType;
import me.jaime29010.combatactionbar.hooks.PluginHook;
import me.jaime29010.combatactionbar.utils.ActionBarHelper;
import me.jaime29010.combatactionbar.utils.ConfigurationManager;
import me.jaime29010.combatactionbar.utils.SoundInfo;
import me.jaime29010.combatactionbar.utils.Sounds;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Main extends JavaPlugin implements Listener {
    private final Map<String, Integer> log = new HashMap<>();
    private FileConfiguration config;
    private int duration = 10;
    private String tagText, untagText, character, bar;
    private SoundInfo tagSound, untagSound;
    private List<String> disabledWorlds = new ArrayList<>();
    private PluginHook hook;

    @Override
    public void onEnable() {
        enablePlugin();
    }

    @Override
    public void onDisable() {
        disablePlugin();
    }

    public void enablePlugin() {
        // Loading the configuration
        config = ConfigurationManager.loadConfig("config.yml", this);

        // Checking if there is any anti combat log plugin installed
        if (config.getBoolean("plugin-check")) {
            if (tryHook()) {
                duration = hook.getDuration();
            } else {
                getLogger().warning("No anti combat log plugin has been found, install one or disable plugin-check in the config");
                setEnabled(false);
                return;
            }
        } else {
            duration = config.getInt("time-untag");
        }

        // Character for the piece of message representing a second
        character = config.getString("character");

        // Setting the bar length
        bar = new String(new char[duration]).replace("\0", character);

        // Setting the tag text
        tagText = config.getString("on-tag.text");

        // Setting the tag sound
        tagSound = "NONE".equals(config.getString("on-tag.sound.type")) ? null : new SoundInfo(
                Sounds.valueOf(config.getString("on-tag.sound.type")).get(),
                (float) config.getDouble("on-tag.sound.volume"),
                (float) config.getDouble("on-tag.sound.pitch"));

        // Setting the untag text
        untagText = config.getString("on-untag.text");

        // Setting the untag sound
        untagSound = "NONE".equals(config.getString("on-untag.sound.type")) ? null : new SoundInfo(
                Sounds.valueOf(config.getString("on-untag.sound.type")).get(),
                (float) config.getDouble("on-untag.sound.volume"),
                (float) config.getDouble("on-untag.sound.pitch"));

        //Setting the disabled worlds
        for (String name : config.getStringList("disabled-worlds")) {
            disabledWorlds.add(name);
        }

        //Initialize the ActionBarHelper
        ActionBarHelper.init(this);

        //Setting up the updater
        if (config.getBoolean("auto-update")) {
            final SpigetUpdate updater = new SpigetUpdate(this, 12923);
            updater.checkForUpdate(new UpdateCallback() {
                @Override
                public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                    if (hasDirectDownload) {
                        if (updater.downloadUpdate()) {
                            getLogger().info("The plugin has successfully updated to version " + newVersion);
                            getLogger().info("The next time you start your server the plugin will have the version");
                        } else {
                            getLogger().warning("Update download failed, reason is " + updater.getFailReason());
                        }
                    }
                }

                @Override
                public void upToDate() {
                    getLogger().info("The plugin is in the latest version available");
                }
            });
        }

        // Registering the events
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void disablePlugin() {
        // Cleaning and cancelling all the tasks
        for (Entry<String, Integer> entry : log.entrySet()) {
            cancelTask(log.remove(entry.getKey()));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            enablePlugin();
            player.sendMessage(ChatColor.GREEN + "The plugin has been reloaded");
        } else {
            sender.sendMessage("This command can only be executed by a player");
        }
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        // The tagging and sending of the action bar
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (config.getBoolean("send-damaged")) sendTag(damaged);
                if (config.getBoolean("send-damager")) sendTag(damager);
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    Player damager = (Player) projectile.getShooter();
                    if (config.getBoolean("ignore-self-damage") && damager.equals(damaged)) return;
                    if (config.getBoolean("send-damaged")) sendTag(damaged);
                    if (config.getBoolean("send-damager")) sendTag(damager);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Removing the player from the log and canceling the task when the player dies
        checkBar(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        // Removing the player from the log and canceling the task when the player quits
        checkBar(event.getPlayer());
    }

    public void checkBar(Player player) {
        // Removes the player from the log and cancels the task
        if (log.containsKey(player.getName())) {
            cancelTask(log.remove(player.getName()));
        }
    }

    public boolean tryHook() {
        for (HookType type : HookType.values()) {
            if (type.check()) {
                try {
                    hook = type.getHook().newInstance();
                    Plugin plugin = type.getPlugin();
                    hook.hook(plugin, getServer().getPluginManager());
                    getLogger().info(String.format("Successfully hooked into %s (%s)", hook.getClass(), plugin.getName()));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    // Adding color to messages
    private String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    // Canceling a task with its id
    public void cancelTask(int taskId) {
        getServer().getScheduler().cancelTask(taskId);
    }

    public void sendTag(Player... players) {
        for (final Player player : players) {
            if (player == null) continue;
            // Check for player in creative mode
            if (player.getGameMode() == GameMode.CREATIVE) continue;

            // Not executing the task if the player is in a disabled world
            if (disabledWorlds.contains(player.getWorld().getName())) continue;

            // Not executing the task if the player has the nobar permission
            if (player.hasPermission("combatactionbar.nobar")) continue;

            // Canceling the previous task associated with the player
            if (log.containsKey(player.getName())) {
                cancelTask(log.remove(player.getName()));
            }

            // Adding and executing the task
            log.put(player.getName(), getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                int times = duration;
                @Override
                public void run() {
                    if (times > 0) {
                        // Canceling the task if the player is in a disabled world
                        if (disabledWorlds.contains(player.getWorld().getName())) {
                            cancelTask(log.remove(player.getName()));
                        }

                        // Sending the tag bar
                        ActionBarHelper.sendActionBar(player, color(tagText
                                // Replacements for the message
                                .replace("{left}", bar.substring(0, times * character.length()))
                                .replace("{right}", bar.substring(times * character.length(), bar.length()))
                                .replace("{time}", String.valueOf(times))));

                        // Sending the tag sound
                        if (tagSound != null) {
                            tagSound.play(player);
                        }

                        // Decreasing the duration count
                        times--;
                    } else {
                        // Sending the untag bar
                        ActionBarHelper.sendActionBar(player, color(untagText));

                        // Sending the untag sound
                        if (untagSound != null) {
                            untagSound.play(player);
                        }

                        // Cancelling the task associated with the player
                        cancelTask(log.remove(player.getName()));
                    }
                }
            }, 0, 20));
        }
    }
}