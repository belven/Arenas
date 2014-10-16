package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import resources.Functions;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.PvPArena;
import belven.arena.events.ArenaSuccessful;

public class PvPArenaTimer extends BukkitRunnable {
	private PvPArena ab;
	public UUID arenaRunID;
	public int nextWave = 0;

	public PvPArenaTimer(PvPArena arenaBlock) {
		ab = arenaBlock;
		arenaRunID = arenaBlock.arenaRunID;
		nextWave = arenaBlock.currentRunTimes;
	}

	@Override
	public void run() {
		if (arenaRunID != ab.arenaRunID || nextWave < ab.currentRunTimes || !ab.isActive) {
			this.cancel();
		} else if (ab.arenaArea.size() == 0 || ab.arenaPlayers.size() == 0) {
			EndArena();
		} else if (ab.currentRunTimes >= ab.maxRunTimes) {
			ArenaSuccessfull();
		} else if (ab.currentRunTimes < ab.maxRunTimes) {
			ab.GoToNextWave();
			this.cancel();
		}
	}

	public void ArenaSuccessfull() {
		// check to see if we need to run other linked arenas
		if (ab.linkedArenas.size() > 0) {
			for (BaseArena lab : ab.linkedArenas) {
				if (lab != null && !lab.isActive) {
					new LinkedArenaTimer(ab, lab)
							.runTaskLater(ab.plugin, Functions.SecondsToTicks(ab.linkedArenaDelay));
				}
			}
		}

		Bukkit.getPluginManager().callEvent(new ArenaSuccessful(ab));
		EndArena();
	}

	public void EndArena() {
		new MessageTimer(ab.arenaPlayers, "Arena " + ab.ArenaName() + " has ended!!").run();

		if (ab.linkedArenas.size() == 0) {
			List<Player> ArenaPlayers = new ArrayList<Player>();
			ArenaPlayers.addAll(ab.arenaPlayers);
			for (Player p : ArenaPlayers) {
				ab.plugin.LeaveArena(p);
				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
		}
		ab.Deactivate();
		this.cancel();
	}
}