package me.august.motd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by August on 4/19/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public class MOTDCommands implements CommandExecutor {

	MOTDPlugin plugin;

	public MOTDCommands(MOTDPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Too few arguments");
			sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GOLD + "/pingmsg [toggle] [clear] [add] (message)");
			return false;

		}
		String type = args[0];

		if(type.equalsIgnoreCase("toggle")) {
			plugin.setShowRecognizedPingMessage(!plugin.isShowingRecognizedPingMessage());
			if(plugin.isShowingRecognizedPingMessage()) {
				sender.sendMessage(ChatColor.GREEN + "Showing recognized ping message");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Not showing recognized ping message");
			}
			return false;
		}
		if(type.equalsIgnoreCase("add")) {
			if(args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Too few arguments");
				sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GOLD + "/pingmsg add (message)");
				return false;
			}
			String msg = "";
			for(int i = 1; i < args.length; i++) {
				msg += i == 1 ? args[i] : " " + args[i];
			}
			plugin.addPingMessage(msg);
			sender.sendMessage(ChatColor.GREEN + "Message added");
			return false;
		}
		if(type.equalsIgnoreCase("clear")) {
			plugin.clearPingMessages();
			sender.sendMessage(ChatColor.GREEN + "Messages cleared");
		}
		return false;
	}

}
