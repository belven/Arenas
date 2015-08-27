package belven.arena.timedevents;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.arenas.BaseArena;
import belven.arena.arenas.PvPArena;
import belven.arena.events.ArenaSuccessful;
import belven.resources.Functions;

public class PvPArenaTimer extends BukkitRunnable {
	private PvPArena ab;
	public UUID arenaRunID;
	public int nextWave = 0;

	public PvPArenaTimer(PvPArena arenaBlock) {
		ab = arenaBlock;
		arenaRunID = arenaBlock.getArenaRunID();
		nextWave = arenaBlock.getCurrentRunTimes();
	}

	@Override
	public void run() {
		if (isTimerVaild()) {
			this.cancel();
		} else if (ab.getCurrentRunTimes() >= ab.getMaxRunTimes()) {
			ArenaSuccessfull();
		} else if (ab.getCurrentRunTimes() < ab.getMaxRunTimes()) {
			ab.ProgressingWave();
			this.cancel();
		}
	}

	private boolean isTimerVaild() {
		return ab.isActive() || arenaRunID == ab.getArenaRunID() || nextWave == ab.getCurrentRunTimes();
	}

	public void ArenaSuccessfull() {
		// check to see if we need to run other linked arenas
		if (ab.getLinkedArenas().size() > 0) {
			for (BaseArena lab : ab.getLinkedArenas()) {
				if (lab != null && !lab.isActive()) {
					new LinkedArenaTimer(ab, lab).runTaskLater(ab.getPlugin(), Functions.SecondsToTicks(ab.getLinkedArenaDelay()));
				}
			}
		}

		Bukkit.getPluginManager().callEvent(new ArenaSuccessful(ab));
		EndArena();
	}

	public void EndArena() {
		new MessageTimer(ab.getArenaPlayers(), "Arena " + ab.ArenaName() + " has ended!!").run();

		// check to see if we need to run other linked arenas
		if (ab.getLinkedArenas().size() > 0) {
			for (BaseArena lab : ab.getLinkedArenas()) {
				if (lab != null && !lab.isActive()) {
					new LinkedArenaTimer(ab, lab).runTaskLater(ab.getPlugin(), Functions.SecondsToTicks(ab.getLinkedArenaDelay()));
				}
			}
		}

		ab.Deactivate();
		this.cancel();
	}
}