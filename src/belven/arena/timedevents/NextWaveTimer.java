package belven.arena.timedevents;

import java.util.Iterator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.Wave;
import belven.arena.blocks.StandardArenaBlock;

public class NextWaveTimer extends BukkitRunnable {
	private StandardArenaBlock arenaBlock;

	public NextWaveTimer(StandardArenaBlock arenaBlock) {
		this.arenaBlock = arenaBlock;
	}

	@Override
	public void run() {
		CleanUpEntites();
		if (arenaBlock.arenaPlayers.size() == 0 || !arenaBlock.isActive) {
			this.cancel();
		} else if (arenaBlock.ArenaEntities.size() == 0
				&& arenaBlock.currentRunTimes < arenaBlock.maxRunTimes) {
			GoToNextWave();
		}
	}

	private void GoToNextWave() {
		arenaBlock.GetPlayersAverageLevel();
		arenaBlock.currentRunTimes++;
		if (arenaBlock.currentRunTimes == 1) {
			new MessageTimer(arenaBlock.arenaPlayers, arenaBlock.name
					+ " has Started!!").run();
		}
		new MessageTimer(arenaBlock.arenaPlayers, arenaBlock.name + " Wave: "
				+ String.valueOf(arenaBlock.currentRunTimes)).run();

		new Wave(arenaBlock);
		new ArenaTimer(arenaBlock).runTaskLater(arenaBlock.plugin,
				arenaBlock.timerPeriod);
		this.cancel();
	}

	private void CleanUpEntites() {
		Iterator<LivingEntity> ArenaEntities = arenaBlock.ArenaEntities
				.iterator();

		while (ArenaEntities.hasNext()) {
			LivingEntity le = ArenaEntities.next();
			if (le != null && le.isDead()) {
				if (le.hasMetadata("ArenaMob")) {
					le.removeMetadata("ArenaMob", arenaBlock.plugin);
				}
				ArenaEntities.remove();
			}
		}
	}
}
