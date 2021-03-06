package belven.arena.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import belven.arena.ArenaManager;
import belven.arena.arenas.BaseArena;
import belven.arena.challengeclasses.Challenge;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.events.ArenaBlockActivatedEvent;
import belven.arena.events.ChallengeComplete;

public class ArenaListener implements Listener {
	public ArenaManager plugin;

	public ArenaListener(ArenaManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onArenaBlockActivatedEvent(ArenaBlockActivatedEvent event) {
		if (plugin.currentArenaBlocks.size() > 0) {
			for (BaseArena ab : plugin.currentArenaBlocks) {
				if (event.GetBlockLocation().equals(ab.getBlockToActivate().getLocation()) && !ab.isActive()) {
					ab.Activate();
					plugin.writeToLog("Arena " + ab.getName() + " was activated.");
				}
			}
		}
	}

	@EventHandler
	public void onArenaBlockBreakEvent(BlockBreakEvent event) {
		if (plugin.IsPlayerInArena(event.getPlayer())) {
			if (plugin.getArena(event.getPlayer()).isActive()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onChallengeComplete(ChallengeComplete event) {
		Challenge cct = event.GetChallengeType();
		for (ChallengeBlock cb : plugin.challengeBlocks) {
			if (cb.challengeType.challengeID == cct.challengeID) {
				cb.GiveRewards();
				break;
			}
		}
	}
}
