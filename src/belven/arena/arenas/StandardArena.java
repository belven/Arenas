package belven.arena.arenas;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.phases.Phase;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;
import belven.resources.Functions;

public class StandardArena extends StandardArenaData {
	public StandardArena(Location startLocation, Location endLocation, String ArenaName, MobToMaterialCollecton mobToMat, ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, mobToMat, Plugin, TimerPeriod);
	}

	public synchronized void GetSpawnArea() {
		getSpawnArea().clear();

		List<Block> tempSpawnArea = Functions.getBlocksBetweenPoints(getSpawnArenaStartLocation(), getSpawnArenaEndLocation());

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
	public synchronized void Activate() {
		try {
			SetPlayers();

			if (getArenaPlayers().size() > 0) {
				setState(ArenaState.Active);
				setArenaRunID(UUID.randomUUID());
				GetSpawnArea();
				GenerateRandomPhases(0.3);
				setTimer(new ArenaTimer(this));
				getTimer().runTaskLater(getPlugin(), 10);
			} else {
				getPlugin().writeToLog("Arena " + getName() + " was started but detected no players");
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog("Arena " + getName() + " failed to go to " + ArenaState.Active.toString() + " state");
		}
	}

	public synchronized void SetAmountOfMobsToSpawn() {
		final int playerCount = getArenaPlayers().size();
		// int spawnSize = getSpawnArea().size() / getDifficulty();
		int totalLevels = 0;

		if (playerCount > 0) {
			setAverageLevel(0);
			setMaxMobCounter(0);

			for (Player p : getArenaPlayers()) {
				totalLevels += p.getLevel();
			}

			if (totalLevels == 0) {
				totalLevels = 1;
			}

			setAverageLevel(Math.round(totalLevels / playerCount));

			int count = Math.round(playerCount * 5);
			count += Math.round(getAverageLevel() / 5);

			if (count > playerCount * _MaxMobCount) {
				count = playerCount * _MaxMobCount;
			}

			setMaxMobCounter(count);
		}
	}

	private Location CanSpawnAt(Location currentLocation) {
		Block currentBlock = currentLocation.getBlock();
		Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
		Block blockAbove = currentBlock.getRelative(BlockFace.UP);

		if (isAir(currentBlock) && isAir(blockAbove) && MobToMat.Contains(blockBelow.getType()) && isWithinArena(currentBlock) && isWithinArena(blockAbove)) {
			return currentLocation;
		} else {
			return null;
		}
	}

	@Override
	public synchronized void ClearingArena() {
		try {
			setState(ArenaState.ClearingArena);
			setArenaRunID(null);
			setCurrentRunTimes(0);
			RestoreArena();

			if (getActivePhase() != null) {
				getActivePhase().deactivate();
			}

			setActivePhase(null);
			getPhases().clear();

			for (LivingEntity le : ArenaEntities) {
				if (!le.isDead()) {
					le.removeMetadata("ArenaMob", getPlugin());
					le.setHealth(0.0);
				}
			}

			ArenaEntities.clear();
			ClearPlayerScoreboards();

			if (getLinkedArenas().size() == 0) {
				for (Player p : getArenaPlayers().subList(0, getArenaPlayers().size())) {
					getPlugin().LeaveArena(p);
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog("Arena " + getName() + " failed to go to active state");
		}
	}

	@Override
	public synchronized void Deactivate() {
		try {
			ClearingArena();

			setState(ArenaState.Deactivated);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog("Arena " + getName() + " failed to go to active state");
		}
	}

	@Override
	public synchronized void ProgressingWave() {
		try {
			if (getCurrentRunTimes() < getMaxRunTimes()) {
				setState(ArenaState.ProgressingWave);
				setCurrentRunTimes(getCurrentRunTimes() + 1);

				SetAmountOfMobsToSpawn();

				if (getCurrentRunTimes() == 1) {
					new MessageTimer(getArenaPlayers(), ArenaName() + " has Started!!").run();
				}

				new Wave(this);
				new MessageTimer(getArenaPlayers(), ArenaName() + " Wave: " + String.valueOf(getCurrentRunTimes())).run();

				setTimer(new ArenaTimer(this));
				getTimer().runTaskLater(getPlugin(), getTimerPeriod());
				setState(ArenaState.Active);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog("Arena " + getName() + " failed to go to " + ArenaState.ProgressingWave.toString() + "  state");
		}
	}

	@Override
	public synchronized void Phased() {
		try {
			if (canTransitionToState(ArenaState.Phased)) {
				setState(ArenaState.Phased);
				Phase activePhase = getPhases().get(getCurrentRunTimes());
				setActivePhase(activePhase);
			} else if (getActivePhase().isActive() && !getActivePhase().isCompleted()) {
				getActivePhase().phaseRanDuration();
				setTimer(new ArenaTimer(this));
				getTimer().runTaskLater(getPlugin(), getActivePhase().getPhaseDuration());
			} else if (canTransitionToState(ArenaState.ProgressingWave)) {
				ProgressingWave();
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog("Arena " + getName() + " failed to go to " + ArenaState.ProgressingWave.toString() + "  state");
		}
	}

	@Override
	public synchronized void PhaseChanged(Phase p) {
		ClearPlayerScoreboards();
		ListIterator<Player> players = getArenaPlayers().listIterator();

		if (p.isActive()) {
			while (players.hasNext()) {
				Player pl = players.next();
				pl.setScoreboard(p.GetPhaseScoreboard());
			}
		} else if (p.isCompleted() && canTransitionToState(ArenaState.ProgressingWave)) {
			ProgressingWave();
		}
	}

	@Override
	public int getDifficulty() {
		return 7;
	}
}
