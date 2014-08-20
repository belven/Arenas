package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.ArenaManager;
import belven.arena.arenas.BaseArena;

public class LinkedArenaTimer extends BukkitRunnable {
	BaseArena parentArena, childArena;
	ArenaManager plugin;

	public LinkedArenaTimer(BaseArena ParentArena, BaseArena ChildArena) {
		plugin = ParentArena.plugin;
		parentArena = ParentArena;
		childArena = ChildArena;
	}

	@Override
	public void run() {
		List<Player> players = new ArrayList<Player>();
		players.addAll(parentArena.arenaPlayers);

		for (Player p : players) {
			parentArena.arenaPlayers.remove(p);
			plugin.PlayersInArenas.remove(p);
			p.teleport(childArena.arenaWarp.getLocation());
		}

		if (parentArena.currentChallengeBlock != null) {
			parentArena.currentChallengeBlock.challengeBlockState.update(true);
		}

		parentArena.RestoreArena();
		childArena.Activate();
		this.cancel();
	}
}
