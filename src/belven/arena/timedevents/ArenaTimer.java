package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import resources.Functions;
import belven.arena.blocks.ArenaBlock;
import belven.arena.events.ArenaSuccessful;

public class ArenaTimer extends BukkitRunnable {
	private ArenaBlock ab;

	public UUID arenaRunID;
	public int nextWave = 0;

	private Random randomGenerator = new Random();

	public ArenaTimer(ArenaBlock arenaBlock) {
		ab = arenaBlock;
		arenaRunID = arenaBlock.arenaRunID;
		nextWave = arenaBlock.currentRunTimes;
	}

	@Override
	public void run() {
		if (arenaRunID != ab.arenaRunID || nextWave < ab.currentRunTimes) {
			this.cancel();
		}

		CleanUpEntites();

		if (ab.arenaArea.size() == 0 || ab.arenaPlayers.size() == 0) {
			EndArena();
		} else if (!ab.isActive) {
			ab.RemoveMobs();
			this.cancel();
		}
		// arena beyond last wave
		else if (ab.currentRunTimes >= ab.maxRunTimes) {
			// we have exceeded the amount of times the arena can run for
			if (ab.ArenaEntities.size() == 0) {
				ArenaSuccessfull();
			} else if (ab.currentRunTimes >= ab.maxRunTimes
					&& ab.currentRunTimes % 7 == 0) {
				SpreadEntities();
				ArenaHasEntitiesLeft();
			} else if (ab.ArenaEntities.size() > 0) {
				ArenaHasEntitiesLeft();
			} else {
				ArenaSuccessfull();
			}
		} else if (ab.spawnArea.size() > 0
				&& ab.currentRunTimes < ab.maxRunTimes) {
			ab.GoToNextWave();
			this.cancel();
		}
	}

	private void SpreadEntities() {
		for (LivingEntity le : ab.ArenaEntities) {
			int randomInt = randomGenerator.nextInt(ab.spawnArea.size());
			Location spawnLocation = ab.spawnArea.get(randomInt).getLocation();

			if (spawnLocation != null) {
				spawnLocation = new Location(spawnLocation.getWorld(),
						spawnLocation.getX() + 0.5, spawnLocation.getY(),
						spawnLocation.getZ() + 0.5);
				le.teleport(spawnLocation);
			}
		}

		new MessageTimer(ab.arenaPlayers, ChatColor.RED + "Scrambling Mobs")
				.run();
	}

	private void ArenaHasEntitiesLeft() {
		ab.GetPlayersAverageLevel();
		ab.currentRunTimes++;
		new MessageTimer(ab.arenaPlayers, "Arena " + ab.ArenaName() + " has "
				+ String.valueOf(ab.ArenaEntities.size()) + " mobs left").run();

		int delay = Functions.SecondsToTicks(10);
		new ArenaTimer(ab).runTaskLater(ab.plugin, delay);
		this.cancel();
	}

	private void CleanUpEntites() {
		Iterator<LivingEntity> ArenaEntities = ab.ArenaEntities.iterator();

		while (ArenaEntities.hasNext()) {
			LivingEntity le = ArenaEntities.next();
			if (le != null && le.isDead()) {
				if (le.hasMetadata("ArenaMob")) {
					le.removeMetadata("ArenaMob", ab.plugin);
				}
				ArenaEntities.remove();
			}
		}
	}

	public void ArenaSuccessfull() {
		new BlockRestorer(Material.REDSTONE_BLOCK, ab.deactivateBlock)
				.runTaskLater(ab.plugin, Functions.SecondsToTicks(1));
		// Give arena rewards
		ab.GiveRewards();

		// check to see if we need to run other linked arenas
		if (ab.linkedArenas.size() > 0) {
			for (ArenaBlock lab : ab.linkedArenas) {
				if (lab != null && !lab.isActive) {
					new LinkedArenaTimer(ab, lab).runTaskLater(ab.plugin,
							Functions.SecondsToTicks(ab.linkedArenaDelay));
				}
			}
		}

		Bukkit.getPluginManager().callEvent(new ArenaSuccessful(ab));
		EndArena();
	}

	public void EndArena() {
		new MessageTimer(ab.arenaPlayers, "Arena " + ab.ArenaName()
				+ " has ended!!").run();
		ab.RemoveMobs();
		ab.isActive = false;

		if (ab.linkedArenas.size() == 0) {
			List<Player> ArenaPlayers = new ArrayList<Player>();
			ArenaPlayers.addAll(ab.arenaPlayers);
			for (Player p : ArenaPlayers) {
				ab.plugin.LeaveArena(p);
				p.setScoreboard(Bukkit.getScoreboardManager()
						.getNewScoreboard());
			}
		}
		this.cancel();
	}
}