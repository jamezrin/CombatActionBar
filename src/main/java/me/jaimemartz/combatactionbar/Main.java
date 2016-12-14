package me.jaimemartz.combatactionbar;

import me.jaimemartz.combatactionbar.utils.ActionBarHelper;
import me.jaimemartz.combatactionbar.utils.SoundInfo;
import me.jaimemartz.faucet.ConfigUtil;
import me.jaimemartz.faucet.Sounds;
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

import java.util.*;
import java.util.Map.Entry;

public final class Main extends JavaPlugin implements Listener {
    private FileConfiguration config;
    private final Map<UUID, BarTask> tasks = new HashMap<>();
    private final List<String> disabledWorlds = new ArrayList<>();
    private int duration = 10;
    private String tagText, untagText, character, bar;
    private SoundInfo tagSound, untagSound;

    @Override
    public void onEnable() {
        enablePlugin();
    }

    @Override
    public void onDisable() {
        disablePlugin();
    }

    public void enablePlugin() {
        getConfig();

        if (config.getBoolean("plugin-check")) {
            if (!tryHook()) {
                getLogger().warning("No anti combat logout plugin has been found, install one or disable plugin-check in the config");
                setEnabled(false);
                return;
            }
        } else {
            duration = config.getInt("time-untag");
        }

        character = config.getString("character");

        StringBuilder buffer = new StringBuilder();
        for (int index = 0; index < duration; index++) {
            buffer.append(character);
        }
        bar = buffer.toString();
        //bar = new String(new char[duration]).replace("\0", character);

        tagText = config.getString("on-tag.text");
        tagSound = "NONE".equals(config.getString("on-tag.sound.type")) ? null : new SoundInfo(
                Sounds.valueOf(config.getString("on-tag.sound.type")).get(),
                (float) config.getDouble("on-tag.sound.volume"),
                (float) config.getDouble("on-tag.sound.pitch"));

        untagText = config.getString("on-untag.text");
        untagSound = "NONE".equals(config.getString("on-untag.sound.type")) ? null : new SoundInfo(
                Sounds.valueOf(config.getString("on-untag.sound.type")).get(),
                (float) config.getDouble("on-untag.sound.volume"),
                (float) config.getDouble("on-untag.sound.pitch"));

        for (String name : config.getStringList("disabled-worlds")) {
            disabledWorlds.add(name);
        }

        ActionBarHelper.init(this);

        if (config.getBoolean("auto-update")) {
            final SpigetUpdate updater = new SpigetUpdate(this, 12923);
            updater.checkForUpdate(new UpdateCallback() {
                @Override
                public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                    if (hasDirectDownload) {
                        if (updater.downloadUpdate()) {
                            getLogger().info("The plugin has successfully updated to version " + newVersion);
                            getLogger().info("The next time you start your server the plugin will have the new version");
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

        getServer().getPluginManager().registerEvents(this, this);
    }

    public void disablePlugin() {
        for (Entry<UUID, BarTask> entry : tasks.entrySet()) {
            BarTask task = entry.getValue();
            task.cancel();
        }
        tasks.clear();
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
    public void on(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Player damager = null;
            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                    if (damager.equals(damaged) && config.getBoolean("ignore-self-damage")) {
                        return;
                    }
                }
            }
            if (damager != null) {
                if (config.getBoolean("send-damaged")) sendTag(damaged);
                if (config.getBoolean("send-damager")) sendTag(damager);
            }
        }
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        cancelTask(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerQuitEvent event) {
        cancelTask(event.getPlayer());
    }

    public boolean tryHook() {
        for (HookType type : HookType.values()) {
            if (type.check()) {
                Plugin plugin = type.getPlugin();
                duration = type.hook();
                getLogger().info(String.format("Successfully hooked into %s (%s)", type.toString(), plugin.getName()));
                return true;
            }
        }
        return false;
    }

    public void sendTag(Player... players) {
        for (final Player player : players) {
            if (isPermitted(player)) {
                cancelTask(player);
                tasks.put(player.getUniqueId(), new BarTask(this, player));
            }
        }
    }

    public boolean isPermitted(Player player) {
        if (player == null) return false;
        if (player.getGameMode() == GameMode.CREATIVE) return false;
        if (disabledWorlds.contains(player.getWorld().getName())) return false;
        if (player.hasPermission("combatactionbar.nobar")) return false;
        return true;
    }

    public void cancelTask(Player player) {
        BarTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public int getDuration() {
        return duration;
    }

    public String getTagText() {
        return tagText;
    }

    public String getUntagText() {
        return untagText;
    }

    public String getCharacter() {
        return character;
    }

    public String getBar() {
        return bar;
    }

    public SoundInfo getTagSound() {
        return tagSound;
    }

    public SoundInfo getUntagSound() {
        return untagSound;
    }

    @Override
    public FileConfiguration getConfig() {
        config = ConfigUtil.loadConfig("config.yml", this);
        return config;
    }

    @Override
    public void reloadConfig() {
        getConfig();
    }

    @Override
    public void saveConfig() {
        ConfigUtil.saveConfig(config, "config.yml", this);
    }

    @Override
    public void saveDefaultConfig() {
        getConfig();
    }
}