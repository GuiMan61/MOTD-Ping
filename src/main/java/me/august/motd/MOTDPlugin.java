package me.august.motd;

import com.mongodb.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by August on 4/18/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public class MOTDPlugin extends JavaPlugin implements Listener {

	MongoClient client;
	DB database;
	private DBCollection collection;
	private boolean showRecognizedPingMessage;
	private List<String> recognizedPingMessages = new ArrayList<>();
	private boolean showAvatar;
	private String avatarURL = "http://cravatar.eu/avatar/{username}/{size}.png";
	private int avatarSize = 64;
	private boolean avatarOverlay;

	@Override
	public void onEnable() {
		try {
			enable();
		} catch(MOTDPluginException e) {
			getLogger().info("Error occurred while enabling plugin");
			e.printStackTrace();
		}
	}

	public void enable() throws MOTDPluginException {
		if(!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		File configFile = new File(getDataFolder(), "config.yml");
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				throw new MOTDPluginException("Could not create config file: " + e.getMessage() + "(" + e.getClass().getName() + ")");
			}
		}
		ConfigurationSection dbConfig = getConfig().getConfigurationSection("db");
		if(dbConfig == null) throw new MOTDPluginException("'db' not found in config");

		String address = dbConfig.getString("address");
		if(address == null) throw new MOTDPluginException("'db.address' not found in config");
		String port = dbConfig.getString("port");
		if(port == null) throw new MOTDPluginException("'db.port' not found in config");

		try {
			client = new MongoClient(address, Integer.parseInt(port));
		} catch(UnknownHostException e) {
			throw new MOTDPluginException("Unable to connect to MongoDB instance at " + address + ":" + port);
		}

		String dbName = dbConfig.getString("database");
		if(dbName == null) throw new MOTDPluginException("'db.database' (database name) not found in config");
		database = client.getDB(dbName);

		String username = dbConfig.getString("username");
		String password = dbConfig.getString("password");

		if(username == null || password == null) {
			getLogger().info("Not attempting to authenticate because 'db.username' or 'db.password' is missing in the config");
		} else {
			if(database.authenticate(username, password.toCharArray())) {
				getLogger().info("Authenticated database '" + database.getName() + "' as " + username + ":" + password);
			} else {
				throw new MOTDPluginException("Could not authenticate database '" + database.getName() + "' as " + username + ":" + password);
			}
		}

		String collName = dbConfig.getString("collection");
		if(collName == null) {
			getLogger().info("No collection name found ('db.collection'), using default: 'motd_ips");
			collName = "motd_ips";
		}

		collection = database.getCollection(collName);

		getServer().getPluginManager().registerEvents(this, this);

		getCommand("pingmsg").setExecutor(new MOTDCommands(this));

		if(getConfig().get("messages") != null) {
			recognizedPingMessages = getConfig().getStringList("messages");
			getLogger().info("messages loaded from config:");
			getLogger().info("" + recognizedPingMessages);
		}

		if(getConfig().get("default_toggle") != null) {
			showRecognizedPingMessage = getConfig().getBoolean("default_toggle");
			getLogger().info("toggle ping messages set to : " + showRecognizedPingMessage);
		}

		showAvatar = getConfig().getBoolean("avatar");
		if(getConfig().get("avatar_size") != null) {
			avatarSize = getConfig().getInt("avatar_size");
		}
		if(getConfig().get("avatar_url") != null) {
			avatarURL = getConfig().getString("avatar_url");
		}
		avatarOverlay = getConfig().getBoolean("avatar_overlay");

	}

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		BasicDBObject query = new BasicDBObject("ip", event.getAddress().toString());
		DBObject result = collection.findOne(query);
		if(result != null) {
			String username = (String) result.get("username");
			getServer().getPluginManager().callEvent(new RecognizedPingEvent(username, event));
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		BasicDBObject remove = new BasicDBObject("ip", event.getAddress().toString());
		BasicDBObject insert = remove.append("username", event.getPlayer().getName());
		collection.remove(remove);
		collection.insert(insert);
	}

	@EventHandler
	public void onRecognizedPing(RecognizedPingEvent event) {
		if(showRecognizedPingMessage) {
			if(recognizedPingMessages.isEmpty()) return;
			String msg = recognizedPingMessages.get(new Random().nextInt(recognizedPingMessages.size()));
			String user = event.getUsername();
			msg = msg.replace("{user}", user);
			msg = msg.replace("{username}", user);
			msg = msg.replace("{player}", user);
			msg = msg.replace("{name}", user);
			event.getPingEvent().setMotd(msg);
			if(showAvatar) {
				try {
					String url = avatarURL;
					url = url.replace("{user}", user);
					url = url.replace("{username}", user);
					url = url.replace("{player}", user);
					url = url.replace("{name}", user);
					url = url.replace("{size}", "" + avatarSize);
					if(avatarOverlay) {
						BufferedImage background = ImageIO.read(new File(getDataFolder(), "background.png"));
						BufferedImage avatar = getAvatar(url);
						CachedServerIcon icon = getServer().loadServerIcon(overlay(avatar, background));
						event.getPingEvent().setServerIcon(icon);
					} else {
						BufferedImage avatar = getAvatar(url);
						CachedServerIcon icon = getServer().loadServerIcon(avatar);
						event.getPingEvent().setServerIcon(icon);
					}
				} catch(Exception ignored) {}
			}
		}
	}

	public boolean isShowingRecognizedPingMessage() {
		return showRecognizedPingMessage;
	}

	public void setShowRecognizedPingMessage(boolean showRecognizedPingMessage) {
		this.showRecognizedPingMessage = showRecognizedPingMessage;
	}

	public void addPingMessage(String msg) {
		recognizedPingMessages.add(msg);
	}

	public void clearPingMessages() {
		recognizedPingMessages.clear();
	}

	public static class MOTDPluginException extends Exception {
		public MOTDPluginException(String message) {
			super(message);
		}
	}


	public static BufferedImage getAvatar(String urlString) throws IOException {
		URL url = new URL(urlString);
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
