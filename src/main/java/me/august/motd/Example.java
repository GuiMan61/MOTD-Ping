package me.august.motd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

import java.awt.image.BufferedImage;

/**
 * Created by August on 4/18/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public class Example implements Listener {

	@EventHandler
	public void onPing(RecognizedPingEvent event) {
		try {
			String url = "http://cravatar.eu/avatar/" + event.getUsername() + "/64.png";
			BufferedImage img = MOTDPlugin.getAvatar(url);
			CachedServerIcon icon = Bukkit.getServer().loadServerIcon(img);
			event.getPingEvent().setServerIcon(icon);
		} catch(Exception e) {
			e.getStackTrace();
		}
		event.getPingEvent().setMotd(ChatColor.GREEN + "Welcome, " + event.getUsername());
	}



}
