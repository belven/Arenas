package belven.arena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import belven.arena.challengeclasses.Challenge;

public class ChallengeComplete extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Challenge ct;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ChallengeComplete(Challenge ct) {
		this.ct = ct;
	}

	public Challenge GetChallengeType() {
		return ct;
	}
}