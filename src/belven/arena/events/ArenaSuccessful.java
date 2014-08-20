package belven.arena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import belven.arena.arenas.BaseArena;

public class ArenaSuccessful extends Event {
	private static final HandlerList handlers = new HandlerList();

	private BaseArena ab;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ArenaSuccessful(BaseArena arenaBlock) {
		this.ab = arenaBlock;
	}

	public BaseArena GetArena() {
		return ab;
	}
}