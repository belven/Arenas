package belven.arena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import belven.arena.blocks.ArenaBlock;

public class ArenaSuccessful extends Event {
	private static final HandlerList handlers = new HandlerList();

	private ArenaBlock ab;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ArenaSuccessful(ArenaBlock arenaBlock) {
		this.ab = arenaBlock;
	}

	public ArenaBlock GetArena() {
		return ab;
	}
}