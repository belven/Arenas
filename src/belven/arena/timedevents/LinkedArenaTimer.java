package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.arenas.BaseArena;

public class LinkedArenaTimer extends BukkitRunnable {
	BaseArena parentArena, childArena;
	ArenaManager plugin;

	public LinkedArenaTimer(BaseArena ParentArena, BaseArena ChildArena) {
		plugin = ParentArena.getPlugin();
		parentArena = ParentArena;
		childArena = ChildArena;
	}

	@Override
	public void run() {
		List<Player> players = new ArrayList<Player>();
		players.addAll(parentArena.getArenaPlayers());

		for (Player p : players) {
			parentArena.getArenaPlayers().remove(p);
			plugin.PlayersInArenas.remove(p);
			Location l = Functions.offsetLocation(childArena.getArenaWarp().getLocation(), 0.5, 0, 0.5);
			p.teleport(l);
		}

		if (parentArena.getCurrentChallengeBlock() != null) {
			parentArena.getCurrentChallengeBlock().challengeBlockState.update(true);
		}

		parentArena.RestoreArena();
		childArena.Activate();
		this.cancel();
	}
}
