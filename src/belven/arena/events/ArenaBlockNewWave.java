package belven.arena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import belven.arena.Wave;
import belven.arena.arenas.BaseArena;

public class ArenaBlockNewWave extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Wave wave;
	private BaseArena arena;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ArenaBlockNewWave(BaseArena ab, Wave newWave) {
		this.wave = newWave;
		this.arena = ab;
	}

	public Wave GetWave() {
		return wave;
	}

	public BaseArena getArena() {
		return arena;
	}

	public void setArena(BaseArena arena) {
		this.arena = arena;
	}
}