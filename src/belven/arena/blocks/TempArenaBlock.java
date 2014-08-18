package belven.arena.blocks;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;

public class TempArenaBlock extends ArenaBlock {

	public TempArenaBlock(Location startLocation, Location endLocation,
			String ArenaName, int Radius, MobToMaterialCollecton mobToMat,
			ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Radius, mobToMat, Plugin,
				TimerPeriod);

		plugin.currentArenaBlocks.add(this);
		Activate();
	}

	public void Activate() {
		// if (arenaPlayers.size() == 0) {
		// Player[] tempPlayers = EntityFunctions.getNearbyPlayersNew(
		// LocationToCheckForPlayers, radius);
		// for (Player p : tempPlayers) {
		// if (!plugin.IsPlayerInArena(p)) {
		// plugin.WarpToArena(p, this);
		// }
		// }
		// }
		SetPlayers();

		if (arenaPlayers.size() != 0) {
			arenaRunID = UUID.randomUUID();
			isActive = true;
			ChallengeBlockWave = new Random().nextInt(maxRunTimes);

			if (ChallengeBlockWave <= 0)
				ChallengeBlockWave = 1;

			RemoveMobs();
			ArenaEntities.clear();
			GetSpawnArea();
			new ArenaTimer(this).runTaskLater(plugin, 10);
		}
	}

	public void Deactivate() {
		arenaRunID = null;
		RestoreArena();
		isActive = false;
		RemoveMobs();
		ArenaEntities.clear();
		plugin.currentArenaBlocks.remove(this);

		if (currentChallengeBlock != null) {
			currentChallengeBlock.challengeBlockState.update(true);
		}

		for (Player p : arenaPlayers) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	public void GoToNextWave() {
		if (arenaPlayers.size() > 0) {
			GetPlayersAverageLevel();
			currentRunTimes++;

			if (currentRunTimes == 1) {
				new MessageTimer(arenaPlayers, ChatColor.RED + name
						+ ChatColor.WHITE + " has Started!!").run();
			}

			if (currentRunTimes == ChallengeBlockWave) {
				currentChallengeBlock = ChallengeBlock.RandomChallengeBlock(
						plugin, this);
			}

			new MessageTimer(arenaPlayers, ChatColor.RED + name
					+ ChatColor.WHITE + " Wave: "
					+ String.valueOf(currentRunTimes)).run();

			new Wave(this);
			new ArenaTimer(this).runTaskLater(plugin, timerPeriod);
		}
	}
}