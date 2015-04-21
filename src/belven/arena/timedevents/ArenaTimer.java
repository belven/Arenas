package belven.arena.timedevents;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.arenas.BaseArena;
import belven.arena.arenas.BaseArenaData.ArenaState;
import belven.arena.arenas.StandardArena;
import belven.arena.events.ArenaSuccessful;
import belven.resources.Functions;

public class ArenaTimer extends BukkitRunnable {
	private StandardArena ab;
	public UUID arenaRunID;

	/**
	 * This is used to ensure that this only runs if the current wave of the arena hasn't changed, i.e. another event has changed it's state
	 * to {@link ArenaState.ProgressingWave}
	 */
	public int nextWave = 0;

	public ArenaTimer(StandardArena arenaBlock) {
		ab = arenaBlock;
		arenaRunID = arenaBlock.getArenaRunID();
		nextWave = arenaBlock.getCurrentRunTimes();
	}

	@Override
	public void run() {
		if (isTimerVaild()) {
			CleanUpEntites();

			if (isBeyondLastWave()) {
				BeyondLastWave();
			} else {
				if (ab.shouldPhase()) {
					ab.Phased();
				} else if (ab.canTransitionToState(ArenaState.ProgressingWave)) {
					ab.ProgressingWave();

					if (ab.getCurrentRunTimes() > nextWave) {
						ab.getPlugin().writeToLog(
								"Arena " + ab.getName() + " has progressed to wave "
										+ String.valueOf(ab.getCurrentRunTimes()));
					}
				}
			}
		}

		this.cancel();
	}

	private boolean isTimerVaild() {
		return ab.isActive() || arenaRunID == ab.getArenaRunID() || nextWave == ab.getCurrentRunTimes();
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
		new MessageTimer(ab.getArenaPlayers(), getMobsLeftString()).run();

		int delay = Functions.SecondsToTicks(10);
		ab.setTimer(new ArenaTimer(ab));
		ab.getTimer().runTaskLater(ab.getPlugin(), delay);
		this.cancel();
	}

	private String getMobsLeftString() {
		return "Arena " + ab.ArenaName() + " has " + String.valueOf(ab.getArenaEntities().size()) + " mobs left";
	}

	private void CleanUpEntites() {
		Iterator<LivingEntity> ArenaEntities = ab.getArenaEntities().iterator();

		while (ArenaEntities.hasNext()) {
			LivingEntity le = ArenaEntities.next();
			if (le == null || le != null && (le.isDead() || !le.isValid())) {
				if (le.hasMetadata("ArenaMob")) {
					le.removeMetadata("ArenaMob", ab.getPlugin());
				}
				ArenaEntities.remove();
			} else if (le != null && !le.getLocation().getBlock().getRelative(BlockFace.DOWN).hasMetadata("ArenaBlock")) {
				Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(ab);
				le.teleport(spawnLocation);
			}
		}
	}

	public synchronized void ArenaSuccessfull() {
		Bukkit.getPluginManager().callEvent(new ArenaSuccessful(ab));

		// Give arena rewards
		ab.GiveRewards();

		ab.getPlugin().writeToLog("Arena " + ab.getName() + " has was completed");
		new MessageTimer(ab.getArenaPlayers(), "Arena " + ab.ArenaName() + " has ended!!").run();

		// check to see if we need to run other linked arenas
		if (ab.getLinkedArenas().size() > 0) {
			for (BaseArena lab : ab.getLinkedArenas()) {
				if (lab != null && !lab.isActive()) {
					new LinkedArenaTimer(ab, lab).runTaskLater(ab.getPlugin(),
							Functions.SecondsToTicks(ab.getLinkedArenaDelay()));
				}
			}
		}

		ab.Deactivate();
		this.cancel();
	}

}
