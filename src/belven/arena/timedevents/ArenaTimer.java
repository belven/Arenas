package belven.arena.timedevents;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.arenas.BaseArena;
import belven.arena.arenas.StandardArena;
import belven.arena.events.ArenaSuccessful;
import belven.resources.Functions;

public class ArenaTimer extends BukkitRunnable {
	private StandardArena ab;

	public UUID arenaRunID;
	public int nextWave = 0;

	public ArenaTimer(StandardArena arenaBlock) {
		ab = arenaBlock;
		arenaRunID = arenaBlock.getArenaRunID();
		nextWave = arenaBlock.getCurrentRunTimes();
	}

	@Override
	public void run() {
		// IFfthe arena or this timer is no longer vaild
		if (!ab.isActive() || arenaRunID != ab.getArenaRunID() || nextWave < ab.getCurrentRunTimes()) {
			this.cancel();
		} else {
			CleanUpEntites();
			if (isBeyondLastWave()) {
				BeyondLastWave();
			} else {
				ab.GoToNextWave();

				if (ab.getCurrentRunTimes() > nextWave) {
					ab.getPlugin().writeToLog(
							"Arena " + ab.getName() + " has progressed to wave "
									+ String.valueOf(ab.getCurrentRunTimes()));
				}

				this.cancel();
			}
		}
	}

	private boolean isBeyondLastWave() {
		return ab.getCurrentRunTimes() >= ab.getMaxRunTimes();
	}

	private void BeyondLastWave() {
		if (ab.getArenaEntities().size() == 0) {
			ArenaSuccessfull();
		} else if (ab.getArenaEntities().size() > 0) {
			if (shouldSpreadEntities()) {
				SpreadEntities();
			}
			ArenaHasEntitiesLeft();
		} else {
			ArenaSuccessfull();
		}
	}

	public boolean shouldSpreadEntities() {
		return ab.getCurrentRunTimes() >= ab.getMaxRunTimes() && ab.getCurrentRunTimes() % 7 == 0;
	}

	private void SpreadEntities() {
		for (LivingEntity le : ab.getArenaEntities()) {
			Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(ab);
			le.teleport(spawnLocation);
		}

		new MessageTimer(ab.getArenaPlayers(), ChatColor.RED + "Scrambling Mobs").run();
	}

	private void ArenaHasEntitiesLeft() {
		ab.SetAmountOfMobsToSpawn();
		ab.setCurrentRunTimes(ab.getCurrentRunTimes() + 1);
		new MessageTimer(ab.getArenaPlayers(), "Arena " + ab.ArenaName() + " has "
				+ String.valueOf(ab.getArenaEntities().size()) + " mobs left").run();

		int delay = Functions.SecondsToTicks(10);
		new ArenaTimer(ab).runTaskLater(ab.getPlugin(), delay);
		this.cancel();
	}

	private void CleanUpEntites() {
		Iterator<LivingEntity> ArenaEntities = ab.getArenaEntities().iterator();

		while (ArenaEntities.hasNext()) {
			LivingEntity le = ArenaEntities.next();
			if (le != null && le.isDead()) {
				if (le.hasMetadata("ArenaMob")) {
					le.removeMetadata("ArenaMob", ab.getPlugin());
				}
				ArenaEntities.remove();
			}
		}
	}

	public synchronized void ArenaSuccessfull() {
		// Give arena rewards
		ab.GiveRewards();

		// check to see if we need to run other linked arenas
		if (ab.getLinkedArenas().size() > 0) {
			for (BaseArena lab : ab.getLinkedArenas()) {
				if (lab != null && !lab.isActive()) {
					new LinkedArenaTimer(ab, lab).runTaskLater(ab.getPlugin(),
							Functions.SecondsToTicks(ab.getLinkedArenaDelay()));
				}
			}
		}

		ab.getPlugin().writeToLog("Arena " + ab.getName() + " has was completed");
		Bukkit.getPluginManager().callEvent(new ArenaSuccessful(ab));
		EndArena();
	}

	public synchronized void EndArena() {
		new MessageTimer(ab.getArenaPlayers(), "Arena " + ab.ArenaName() + " has ended!!").run();
		ab.Deactivate();
		this.cancel();
	}
}