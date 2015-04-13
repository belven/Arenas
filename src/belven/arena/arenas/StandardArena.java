package belven.arena.arenas;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;
import belven.resources.Functions;

public class StandardArena extends StandardArenaData {
	public StandardArena(Location startLocation, Location endLocation, String ArenaName,
			MobToMaterialCollecton mobToMat, ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, mobToMat, Plugin, TimerPeriod);
	}

	public void GetSpawnArea() {
		getSpawnArea().clear();

		List<Block> tempSpawnArea = Functions.getBlocksBetweenPoints(getSpawnArenaStartLocation(),
				getSpawnArenaEndLocation());

		if (tempSpawnArea != null && tempSpawnArea.size() > 0) {
			for (Block b : tempSpawnArea) {
				Location spawnLocation = CanSpawnAt(b.getLocation());
				if (spawnLocation != null) {
					getSpawnArea().add(spawnLocation.getBlock());
				}
			}
		}
	}

	@Override
	public void Activate() {
		SetPlayers();

		if (getArenaPlayers().size() != 0) {
			setArenaRunID(UUID.randomUUID());
			setActive(true);
			RemoveMobs();
			ArenaEntities.clear();
			GetSpawnArea();
			GenerateRandomPhases(0.5);
			new ArenaTimer(this).runTaskLater(getPlugin(), 10);
		}
	}

	public void GetPlayersAverageLevel() {
		if (getArenaPlayers().size() == 0) {
			return;
		}

		int totalLevels = 0;
		setAverageLevel(0);
		setMaxMobCounter(0);

		for (Player p : getArenaPlayers()) {
			totalLevels += p.getLevel();
		}

		if (totalLevels == 0) {
			totalLevels = 1;
		}

		setAverageLevel(totalLevels / getArenaPlayers().size());
		setMaxMobCounter(totalLevels / getArenaPlayers().size() + getArenaPlayers().size() * 5);

		if (getMaxMobCounter() > getArenaPlayers().size() * _MaxMobCount) {
			setMaxMobCounter(getArenaPlayers().size() * _MaxMobCount);
		}
	}

	private Location CanSpawnAt(Location currentLocation) {
		Block currentBlock = currentLocation.getBlock();
		Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
		Block blockAbove = currentBlock.getRelative(BlockFace.UP);

		if (isAir(currentBlock) && isAir(blockAbove) && MobToMat.Contains(blockBelow.getType())
				&& isWithinArena(currentBlock) && isWithinArena(blockAbove)) {
			return currentLocation;
		} else {
			return null;
		}
	}

	public void RemoveMobs() {
		setCurrentRunTimes(0);
		for (LivingEntity le : ArenaEntities) {
			if (!le.isDead()) {
				le.removeMetadata("ArenaMob", getPlugin());
				le.setHealth(0.0);
			}
		}
	}

	@Override
	public void Deactivate() {
		setArenaRunID(null);
		RestoreArena();
		setActive(false);
		RemoveMobs();
		ArenaEntities.clear();

		if (getCurrentChallengeBlock() != null) {
			getCurrentChallengeBlock().challengeBlockState.update(true);
		}
		ListIterator<Player> players = getArenaPlayers().listIterator();

		while (players.hasNext()) {
			Player p = players.next();
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	@Override
	public void GoToNextWave() {
		if (getArenaPlayers().size() > 0) {
			GetPlayersAverageLevel();

			// We only progress to another wave if the there is no phase or it's not active
			if (getActivePhase() == null || !getActivePhase().isActive()) {
				setCurrentRunTimes(getCurrentRunTimes() + 1);

				if (getCurrentRunTimes() == 1) {
					new MessageTimer(getArenaPlayers(), ArenaName() + " has Started!!").run();
				}

				// We only move to another wave if there is no phase this wave
				if (getPhases().containsKey(getCurrentRunTimes())) {
					setActivePhase(getPhases().get(getCurrentRunTimes()));
					getPhases().get(getCurrentRunTimes()).activate();
					new MessageTimer(getArenaPlayers(), "A phase has begun").run();
				}

				new Wave(this);
				new MessageTimer(getArenaPlayers(), ArenaName() + " Wave: " + String.valueOf(getCurrentRunTimes()))
						.run();
				new ArenaTimer(this).runTaskLater(getPlugin(), getTimerPeriod());
			} else if (getActivePhase() != null && getActivePhase().isActive()) {
				for (Player p : getPlayers()) {
					p.setScoreboard(getActivePhase().GetPhaseScoreboard());
				}
				new ArenaTimer(this).runTaskLater(getPlugin(), Functions.SecondsToTicks(2));
			}
		}
	}
}