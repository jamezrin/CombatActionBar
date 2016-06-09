package me.jaime29010.combatactionbar;

import me.jaime29010.combatactionbar.hooks.HookType;
import me.jaime29010.combatactionbar.hooks.PluginHook;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Main extends JavaPlugin implements Listener {
    private final Map<String, Integer> log = new HashMap<>();
    private FileConfiguration config;
    private int duration = 10;
    private String nmsver;
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
                Sound.valueOf(config.getString("on-tag.sound.type")),
                (float) config.getDouble("on-tag.sound.volume"),
                (float) config.getDouble("on-tag.sound.pitch"));

        // Setting the untag text
        untagText = config.getString("on-untag.text");

        // Setting the untag sound
        untagSound = "NONE".equals(config.getString("on-untag.sound.type")) ? null : new SoundInfo(
                Sound.valueOf(config.getString("on-untag.sound.type")),
                (float) config.getDouble("on-untag.sound.volume"),
                (float) config.getDouble("on-untag.sound.pitch"));

        //Setting the disabled worlds
        for (String name : config.getStringList("disabled-worlds")) {
            disabledWorlds.add(name);
        }

        // Getting the nms package
        nmsver = getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

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
                sendTag(damaged);
                if (config.getBoolean("send-damager")) sendTag(damager);
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    Player damager = (Player) projectile.getShooter();
                    if (damager.equals(damaged)) return;
                    sendTag(damaged);
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
                    hook.hook(type.getPlugin(), getServer().getPluginManager());
                    getLogger().info("Hooked into " + hook.getClass());
                } catch (InstantiationException | IllegalAccessException e) {
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
                        sendActionBar(player, color(tagText
                                // Replacements for the message
                                .replace("{left}", bar.substring(0, times * character.length()))
                                .replace("{right}", bar.substring(times * character.length(), bar.length()))
                                .replace("{duration}", String.valueOf(times))));

                        // Sending the tag sound
                        if (tagSound != null) {
                            tagSound.play(player);
                        }

                        // Decreasing the duration count
                        times--;
                    } else {
                        // Sending the untag bar
                        sendActionBar(player, color(untagText));

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

    // Method taken from ActionBarAPI
    private void sendActionBar(Player player, String message) {
        try {
            Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
            Object p = c1.cast(player);
            Object ppoc;
            Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
            Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
            if (nmsver.equalsIgnoreCase("v1_8_R1") || (!nmsver.startsWith("v1_8_") && !nmsver.startsWith("v1_9_") && !nmsver.startsWith("v1_10_"))) {
                Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
                Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
                Method m3 = c2.getDeclaredMethod("a", String.class);
                Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
                ppoc = c4.getConstructor(c3, byte.class).newInstance(cbc, (byte) 2);
            } else {
                Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
                Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
                Object o = c2.getConstructor(String.class).newInstance(message);
                ppoc = c4.getConstructor(c3, byte.class).newInstance(o, (byte) 2);
            }
            Method m1 = c1.getDeclaredMethod("getHandle");
            Object h = m1.invoke(p);
            Field f1 = h.getClass().getDeclaredField("playerConnection");
            Object pc = f1.get(h);
            Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
            m5.invoke(pc, ppoc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}