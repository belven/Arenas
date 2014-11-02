package belven.arena.timedevents;

import java.util.Iterator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.Wave;
import belven.arena.arenas.StandardArena;

public class NextWaveTimer extends BukkitRunnable {
	private StandardArena arenaBlock;

	public NextWaveTimer(StandardArena arenaBlock) {
		this.arenaBlock = arenaBlock;
	}

	@Override
	public void run() {
		CleanUpEntites();
		if (arenaBlock.getArenaPlayers().size() == 0 || !arenaBlock.isActive()) {
			this.cancel();
		} else if (arenaBlock.getArenaEntities().size() == 0
				&& arenaBlock.getCurrentRunTimes() < arenaBlock.getMaxRunTimes()) {
			GoToNextWave();
		}
	}

	private void GoToNextWave() {
		arenaBlock.GetPlayersAverageLevel();
		arenaBlock.setCurrentRunTimes(arenaBlock.getCurrentRunTimes() + 1);
		if (arenaBlock.getCurrentRunTimes() == 1) {
			new MessageTimer(arenaBlock.getArenaPlayers(), arenaBlock.getName() + " has Started!!").run();
		}
		new MessageTimer(arenaBlock.getArenaPlayers(), arenaBlock.getName() + " Wave: "
				+ String.valueOf(arenaBlock.getCurrentRunTimes())).run();

		new Wave(arenaBlock);
		new ArenaTimer(arenaBlock).runTaskLater(arenaBlock.getPlugin(), arenaBlock.getTimerPeriod());
		this.cancel();
	}

	private void CleanUpEntites() {
		Iterator<LivingEntity> ArenaEntities = arenaBlock.getArenaEntities().iterator();

		while (ArenaEntities.hasNext()) {
			LivingEntity le = ArenaEntities.next();
			if (le != null && le.isDead()) {
				if (le.hasMetadata("ArenaMob")) {
					le.removeMetadata("ArenaMob", arenaBlock.getPlugin());
				}
				ArenaEntities.remove();
			}
		}
	}
}
