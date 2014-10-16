package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import resources.Functions;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.StandardArena;
import belven.arena.events.ArenaSuccessful;

public class ArenaTimer extends BukkitRunnable {
	private StandardArena ab;

	public UUID arenaRunID;
	public int nextWave = 0;

	public ArenaTimer(StandardArena arenaBlock) {
		ab = arenaBlock;
		arenaRunID = arenaBlock.arenaRunID;
		nextWave = arenaBlock.currentRunTimes;
	}

	@Override
	public void run() {
		if (arenaRunID != ab.arenaRunID || nextWave < ab.currentRunTimes) {
			this.cancel();
		} else if (!ab.isActive) {
			ab.RemoveMobs();
			this.cancel();
		} else if (ab.arenaArea.size() == 0 || ab.arenaPlayers.size() == 0) {
			EndArena();
		} else {
			CleanUpEntites();
			if (ab.currentRunTimes >= ab.maxRunTimes) {
				BeyondLastWave();
			} else {
				ab.GoToNextWave();
				this.cancel();
			}
		}
	}

	private void BeyondLastWave() {
		if (ab.ArenaEntities.size() == 0) {
			ArenaSuccessfull();
		} else if (ab.currentRunTimes >= ab.maxRunTimes && ab.currentRunTimes % 7 == 0) {
			SpreadEntities();
			ArenaHasEntitiesLeft();
		} else if (ab.ArenaEntities.size() > 0) {
			ArenaHasEntitiesLeft();
		} else {
			ArenaSuccessfull();
		}
	}

	private void SpreadEntities() {
		for (LivingEntity le : ab.ArenaEntities) {
			Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(ab);
			le.teleport(spawnLocation);
		}

		new MessageTimer(ab.arenaPlayers, ChatColor.RED + "Scrambling Mobs").run();
	}

	private void ArenaHasEntitiesLeft() {
		ab.GetPlayersAverageLevel();
		ab.currentRunTimes++;
		new MessageTimer(ab.arenaPlayers, "Arena " + ab.ArenaName() + " has " + String.valueOf(ab.ArenaEntities.size())
				+ " mobs left").run();

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
		new BlockRestorer(Material.REDSTONE_BLOCK, ab.deactivateBlock).runTaskLater(ab.plugin,
				Functions.SecondsToTicks(1));
		// Give arena rewards
		ab.GiveRewards();

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