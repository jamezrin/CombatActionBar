package me.jaime29010.combatactionbar;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
	private final Main main;
	public MainCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			main.disablePlugin();
			main.reloadConfig();
			main.enablePlugin();
			player.sendMessage(ChatColor.GREEN + "The plugin has been reloaded");
		} else {
			sender.sendMessage("This command can only be executed by a player");
		}
		return true;
	}
}
