package belven.arena.timedevents;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
		if (arenaRunID != ab.getArenaRunID() || nextWave < ab.getCurrentRunTimes()) {
			this.cancel();
		} else if (!ab.isActive()) {
			ab.RemoveMobs();
			this.cancel();
		} else if (ab.getArenaArea().size() == 0 || ab.getArenaPlayers().size() == 0) {
			EndArena();
		} else {
			CleanUpEntites();
			if (ab.getCurrentRunTimes() >= ab.getMaxRunTimes()) {
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

	private void BeyondLastWave() {
		if (ab.getArenaEntities().size() == 0) {
			ArenaSuccessfull();
		} else if (ab.getCurrentRunTimes() >= ab.getMaxRunTimes() && ab.getCurrentRunTimes() % 7 == 0) {
			SpreadEntities();
			ArenaHasEntitiesLeft();
		} else if (ab.getArenaEntities().size() > 0) {
			ArenaHasEntitiesLeft();
		} else {
			ArenaSuccessfull();
		}
	}

	private void SpreadEntities() {
		for (LivingEntity le : ab.getArenaEntities()) {
			Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(ab);
			le.teleport(spawnLocation);
		}

		new MessageTimer(ab.getArenaPlayers(), ChatColor.RED + "Scrambling Mobs").run();
	}

	private void ArenaHasEntitiesLeft() {
		ab.GetPlayersAverageLevel();
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

		new BlockRestorer(Material.REDSTONE_BLOCK, ab.getDeactivateBlock()).runTaskLater(ab.getPlugin(),
				Functions.SecondsToTicks(1));

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