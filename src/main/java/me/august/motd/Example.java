package me.august.motd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

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
			BufferedImage img = loadImage(event.getUsername());
			CachedServerIcon icon = Bukkit.getServer().loadServerIcon(img);
			event.getPingEvent().setServerIcon(icon);
		} catch(Exception e) {
			e.getStackTrace();
		}
		event.getPingEvent().setMotd(ChatColor.GREEN + "Welcome, " + event.getUsername());
	}

	public static BufferedImage loadImage(String username) throws IOException {
		String avatarURL = "http://cravatar.eu/head/" + username + "/64.png";
		URL url = new URL(avatarURL);
		return ImageIO.read(url);
	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height, int type) {
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}

	public static int getType(BufferedImage image) {
		return image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
	}

	public BufferedImage overlay(BufferedImage overlay, BufferedImage bg) throws Exception {
		int w = Math.max(bg.getWidth(), overlay.getWidth());
		int h = Math.max(bg.getHeight(), overlay.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = combined.getGraphics();
		g.drawImage(bg, 0, 0, null);
		g.drawImage(overlay, 0, 0, null);
		return combined;
	}

}
