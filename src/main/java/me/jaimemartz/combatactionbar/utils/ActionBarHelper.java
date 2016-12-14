package me.jaimemartz.combatactionbar.utils;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import me.jaimemartz.combatactionbar.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ActionBarHelper {
    private static boolean hooked = false;
    private static String nmsver = null;

    public static void init(Main main) {
        Plugin plugin = main.getServer().getPluginManager().getPlugin("ActionBarAPI");
        if (plugin != null && plugin.isEnabled()) {
            plugin.getLogger().info("ActionBarAPI has been found, it will be used to send the bar");
            hooked = true;
        } else {
            nmsver = main.getServer().getClass().getPackage().getName();
            nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
            hooked = false;
        }
    }

    public static boolean hasHooked() {
        return hooked;
    }

    public static void sendActionBar(Player player, String message) {
        if (!hasHooked()) {
            try {
                Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
                Object p = c1.cast(player);
                Object ppoc;
                Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
                Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
                if (nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.startsWith("v1_7_")) {
                    Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
                    Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
                    Method m3 = c4.getDeclaredMethod("a", String.class);
                    Object cbc = c5.cast(m3.invoke(c4, "{\"text\": \"" + message + "\"}"));
                    ppoc = c2.getConstructor(c5, byte.class).newInstance(cbc, (byte) 2);
                } else {
                    Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
                    Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
                    Object o = c4.getConstructor(String.class).newInstance(message);
                    ppoc = c2.getConstructor(c5, byte.class).newInstance(o, (byte) 2);
                }
                Method m1 = c1.getDeclaredMethod("getHandle");
                Object h = m1.invoke(p);
                Field f1 = h.getClass().getDeclaredField("playerConnection");
                Object pc = f1.get(h);
                Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c3);
                m5.invoke(pc, ppoc);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            ActionBarAPI.sendActionBar(player, message);
        }
    }
}
