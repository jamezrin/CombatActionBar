package me.jaime29010.combatactionbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	private final Map<String, Integer> log = new HashMap<String, Integer>();
	private int seconds = 10;
	private String nmsver;
	private String tagText, untagText, character, bar;
	private SoundInfo tagSound, untagSound;
	private List<World> disabledWorlds = new ArrayList<>();

	@Override
	public void onEnable() {
		enablePlugin();
	}

	@Override
	public void onDisable() {
		disablePlugin();
	}

	public void enablePlugin() {
		// Registering the events
		getServer().getPluginManager().registerEvents(this, this);

		// Saving the default config
		saveDefaultConfig();

		// Getting the seconds
		seconds = getConfig().getInt("time-untag");

		// Character for the piece of message representing a second
		character = getConfig().getString("character");

		// Setting the bar length
		bar = new String(new char[seconds]).replace("\0", character);

		// Setting the tag text
		tagText = getConfig().getString("on-tag.text");

		// Setting the tag sound
		tagSound = "NONE".equals(getConfig().getString("on-tag.sound.type")) ? null : new SoundInfo(
				Sound.valueOf(getConfig().getString("on-tag.sound.type")),
						(float) getConfig().getDouble("on-tag.sound.volume"),
						(float) getConfig().getDouble("on-tag.sound.pitch"));

		// Setting the untag text
		untagText = getConfig().getString("on-untag.text");

		// Setting the untag sound
		untagSound = "NONE".equals(getConfig().getString("on-untag.sound.type")) ? null : new SoundInfo(
				Sound.valueOf(getConfig().getString("on-untag.sound.type")),
						(float) getConfig().getDouble("on-untag.sound.volume"),
						(float) getConfig().getDouble("on-untag.sound.pitch"));
		
		//Setting the disabled worlds
		for(String name : getConfig().getStringList("disabled-worlds")) {
			World world = getServer().getWorld(name);
			if(name != null)
				disabledWorlds.add(world);
		}
		// Getting the nms package
		nmsver = getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		// Registering the command
		getCommand("combatactionbar").setExecutor(new MainCommand(this));

		// Checking if there is any anti combat log plugin installed
		if (getConfig().getBoolean("plugin-check")) {
			if (!checkCompatiblePlugin()) {
				getLogger().warning("No anti combat log plugin has been found, install one or disable plugin-check in the config");
				setEnabled(false);
			}
		}
	}

	public void disablePlugin() {
		// Cleaning and cancelling all the tasks
		for (Entry<String, Integer> entry : log.entrySet()) {
			cancelTask(log.remove(entry.getKey()));
		}
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
				if (getConfig().getBoolean("send-damager")) sendTag(damager);
			} else if (event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if (projectile.getShooter() instanceof Player) {
					Player damager = (Player) projectile.getShooter();
					if (damager.equals(damaged)) return;
					sendTag(damaged);
					if (getConfig().getBoolean("send-damager")) sendTag(damager);
				}
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		// Removing the player from the log and canceling the task when the
		// player dies
		checkBar(event.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		// Removing the player from the log and canceling the task when the
		// player quits
		checkBar(event.getPlayer());
	}

	public void checkBar(Player player) {
		// Removes the player from the log and cancels the task
		if (log.containsKey(player.getName())) {
			cancelTask(log.remove(player.getName()));
		}
	}

	public boolean checkCompatiblePlugin() {
		// Checking for all the compatible plugins classes
		for (String name : new String[] { "CombatLog", "CombatTag", "CombatTagPlus", "PvPManager", "AntiCombatLog" }) {
			if (getServer().getPluginManager().getPlugin(name) != null) return true;
		}
		return false;
	}

	// Adding color to messages
	public String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	// Canceling a task with its id
	public void cancelTask(int taskId) {
		getServer().getScheduler().cancelTask(taskId);
	}

	public void sendTag(Player... players) {
		for (final Player player : players) {
			if (player.getGameMode().equals(GameMode.CREATIVE)) return;
			if (disabledWorlds.contains(player.getWorld())) return;
			// Canceling the previous task associated with the player
			if (log.containsKey(player.getName())) cancelTask(log.remove(player.getName()));
			log.put(player.getName(), getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				int times = seconds;
				@Override
				public void run() {
					if (times > 0) {
						// Sending the tag bar
						sendActionBar(player, color(tagText
								
						// Replacements for the message
						.replace("{left}", bar.substring(0, times * character.length()))
						.replace("{right}", bar.substring(times * character.length(), bar.length()))
						.replace("{seconds}", Integer.toString(times))));
						
						// Sending the tag sound
						if (tagSound != null) tagSound.play(player);
						
						// Decreasing the seconds count
						times--;
					} else {
						// Sending the untag bar
						sendActionBar(player, color(untagText));
						
						// Sending the untag sound
						if (untagSound != null) untagSound.play(player);
						
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
			Object ppoc = null;
			Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
			if (nmsver.equalsIgnoreCase("v1_8_R1") || !nmsver.startsWith("v1_8_")) {
				Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
				Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				Method m3 = c2.getDeclaredMethod("a", new Class<?>[] { String.class });
				Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(new Object[] { cbc, (byte) 2 });
			} else {
				Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
				Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				Object o = c2.getConstructor(new Class<?>[] { String.class }).newInstance(new Object[] { message });
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(new Object[] { o, (byte) 2 });
			}
			Method m1 = c1.getDeclaredMethod("getHandle", new Class<?>[] {});
			Object h = m1.invoke(p);
			Field f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", new Class<?>[] { c5 });
			m5.invoke(pc, ppoc);
		} catch (Exception ex) {
			getLogger().severe("The plugin is not compatible with the server, disabling the plugin...");
			getLogger().severe("Contact the author of this plugin with this error to fix it");
			ex.printStackTrace();
			setEnabled(false);
		}
	}
}