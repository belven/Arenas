package belven.arena.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;

public class StandardArenaBlock extends ArenaBlock {

	public BossMob bm = new BossMob();
	public MobToMaterialCollecton MobToMat = new MobToMaterialCollecton();
	public List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
	public EliteMobCollection emc = new EliteMobCollection(this);

	public StandardArenaBlock(Location startLocation, Location endLocation,
			String ArenaName, int Radius, MobToMaterialCollecton mobToMat,
			ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Radius, Plugin,
				TimerPeriod);
		MobToMat = mobToMat;
		type = ArenaTypes.Standard;
	}

	public void GetSpawnArea() {
		Location spawnLocation;
		spawnArea.clear();

		List<Block> tempSpawnArea = Functions.getBlocksBetweenPoints(
				spawnArenaStartLocation, spawnArenaEndLocation);

		if (tempSpawnArea != null && tempSpawnArea.size() > 0) {
			for (Block b : tempSpawnArea) {
				spawnLocation = b.getLocation();
				spawnLocation = CanSpawnAt(spawnLocation);
				if (spawnLocation != null && !b.equals(spawnArenaStartLocation)) {
					Block spawnBlock = spawnLocation.getBlock();
					spawnArea.add(spawnBlock);
				}
			}
		}
	}

	@Override
	public void Activate() {
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

	private Location CanSpawnAt(Location currentLocation) {
		Block currentBlock = currentLocation.getBlock();
		Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
		Block blockAbove = currentBlock.getRelative(BlockFace.UP);

		if (currentBlock.getType() == Material.AIR
				&& blockAbove.getType() == Material.AIR
				&& MobToMat.Contains(blockBelow.getType())) {
			return currentLocation;
		} else {
			return null;
		}
	}

	public void RemoveMobs() {
		currentRunTimes = 0;
		for (LivingEntity le : ArenaEntities) {
			if (!le.isDead()) {
				le.removeMetadata("ArenaMob", plugin);
				le.setHealth(0.0);
			}
		}
	}

	@Override
	public void Deactivate() {
		arenaRunID = null;
		RestoreArena();
		isActive = false;
		RemoveMobs();
		ArenaEntities.clear();

		if (currentChallengeBlock != null) {
			currentChallengeBlock.challengeBlockState.update(true);
		}

		for (Player p : arenaPlayers) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	@Override
	public void GoToNextWave() {
		if (arenaPlayers.size() > 0) {
			GetPlayersAverageLevel();
			currentRunTimes++;

			if (currentRunTimes == 1) {
				new MessageTimer(arenaPlayers, ArenaName() + " has Started!!")
						.run();
			}

			if (currentRunTimes == ChallengeBlockWave) {
				currentChallengeBlock = ChallengeBlock.RandomChallengeBlock(
						plugin, this);
			}

			new MessageTimer(arenaPlayers, ArenaName() + " Wave: "
					+ String.valueOf(currentRunTimes)).run();
			new Wave(this);
			new ArenaTimer(this).runTaskLater(plugin, timerPeriod);
		}
	}
}