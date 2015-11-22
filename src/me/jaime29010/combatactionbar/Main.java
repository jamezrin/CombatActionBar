package me.jaime29010.combatactionbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
	private String bar;
	private String nmsver;
	private String message, logoutMessage, character;
	
	@Override
	public void onEnable() {
		//Registering the events
		getServer().getPluginManager().registerEvents(this, this);
	    
	    //Saving the default config
	    saveDefaultConfig();
	    
	    //Getting the seconds
	    seconds = getConfig().getInt("timeout");
	    
	    //Character for the piece of message representing a second
	    character = getConfig().getString("character");
	    
	    //Setting the bar length
	    bar = new String(new char[seconds]).replace("\0", character);
	    
	    //Setting the message
	    message = getConfig().getString("message");
	    
	    //Setting the final message
	    logoutMessage = getConfig().getString("safe-logout-message");
	    
	    //Getting the nms package
	  	nmsver = Bukkit.getServer().getClass().getPackage().getName();
	  	nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
	  	
	  	//Checking if there is any anti combat log plugin installed
	  	if(getConfig().getBoolean("plugin-check")) {
	  		if(!checkCompatiblePlugin()) {
	  			getLogger().warning("No anti combat log plugin has been found, install one or disable plugin-check in the config");
	  			setEnabled(false);
	  		}
	  	}
	}
	
	@Override
	public void onDisable() {
		for(Entry<String, Integer> entry : log.entrySet()) {
			cancelTask(log.remove(entry.getKey()));
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onHit(EntityDamageByEntityEvent event) {
		if(event.isCancelled()) return;
		if (event.getEntity() instanceof Player) {
			Player damaged = (Player) event.getEntity();
			if(event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				sendTag(damaged, damager);
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					Player damager = (Player) projectile.getShooter();
					sendTag(damaged, damager);
				}
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		checkBar(event.getEntity());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		checkBar(event.getPlayer());
	}
	
	public String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
	public void checkBar(Player player) {
		if (log.containsKey(player.getName())) {
			cancelTask(log.remove(player.getName()));
		}
	}
	
	public boolean checkCompatiblePlugin() {
		for(String clazz : Arrays.asList(
  				"com.jackproehl.plugins.CombatLog", 
  				"net.techcable.combattag.CombatTag", 
  				"com.mlgprocookie.acl.main",
  				"me.NoChance.PvPManager.PvPManager",
  				"net.minelink.ctplus.CombatTagPlus")) {
  			if(ClassUtils.isPresent(clazz)) return true;
  		}
		return false;
	}
	
	public void cancelTask(int taskId) {
		getServer().getScheduler().cancelTask(taskId);
	}
	
	public void sendTag(Player... players) {
		for(final Player player : players) {
			if(log.containsKey(player.getName())) cancelTask(log.remove(player.getName()));
			log.put(player.getName(), getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				int times = seconds;
				@Override
				public void run() {
					if (times > 0) {
						int time = times--;
						sendActionBar(player, color(String.format(message, bar.substring(0, time * 2), bar.substring(time * 2, bar.length()), time)));
					} else {
						cancelTask(log.remove(player.getName()));
						sendActionBar(player, color(logoutMessage));
					}
				}
			}, 0, 20));
		}
	}
	
	private void sendActionBar(Player player, String message){
    	try {
    		Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
    		Object p = c1.cast(player);
    		Object ppoc = null;
    		Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
    		Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
    		if (nmsver.equalsIgnoreCase("v1_8_R1") || !nmsver.startsWith("v1_8_")) {
        		Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
        		Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
        		Method m3 = c2.getDeclaredMethod("a", new Class<?>[] {String.class});
        		Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
        		ppoc = c4.getConstructor(new Class<?>[] {c3, byte.class}).newInstance(new Object[] {cbc, (byte) 2});
    		} else {
    			Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
        		Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
    			Object o = c2.getConstructor(new Class<?>[] {String.class}).newInstance(new Object[] {message});
        		ppoc = c4.getConstructor(new Class<?>[] {c3, byte.class}).newInstance(new Object[] {o, (byte) 2});
    		}
    		Method m1 = c1.getDeclaredMethod("getHandle", new Class<?>[] {});
    		Object h = m1.invoke(p);
    		Field f1 = h.getClass().getDeclaredField("playerConnection");
    		Object pc = f1.get(h);
    		Method m5 = pc.getClass().getDeclaredMethod("sendPacket",new Class<?>[] {c5});
    		m5.invoke(pc, ppoc);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}