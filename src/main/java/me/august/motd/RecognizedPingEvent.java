package me.august.motd;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * Created by August on 4/18/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public class RecognizedPingEvent extends Event {

	private static HandlerList handlers = new HandlerList();

	private String username;
	private ServerListPingEvent pingEvent;

	public RecognizedPingEvent(String username, ServerListPingEvent pingEvent) {
		this.username = username;
		this.pingEvent = pingEvent;
	}

	public String getUsername() {
		return username;
	}

	public ServerListPingEvent getPingEvent() {
		return pingEvent;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
