package belven.arena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import belven.arena.Wave;

public class ArenaBlockNewWave extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Wave wave;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ArenaBlockNewWave(Wave newWave) {
		this.wave = newWave;
	}

	public Wave GetWave() {
		return wave;
	}
}