package me.jaime29010.combatactionbar.utils;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import me.jaime29010.combatactionbar.Main;
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
        if (hasHooked()) {
            ActionBarAPI.sendActionBar(player, message);
        } else {
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
}
