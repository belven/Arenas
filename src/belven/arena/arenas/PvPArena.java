package belven.arena.arenas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import belven.arena.ArenaManager;
import belven.arena.phases.Phase;
import belven.arena.timedevents.MessageTimer;
import belven.arena.timedevents.PvPArenaTimer;
import belven.resources.Functions;

public class PvPArena extends BaseArena {

	public HashMap<String, Integer> Lives = new HashMap<String, Integer>();
	public List<Material> spawnMats = new ArrayList<Material>();
	public int lives = 0;

	public PvPArena(Location startLocation, Location endLocation, String ArenaName, int Radius, ArenaManager Plugin,
			Material m, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Plugin, TimerPeriod);
		spawnMats.add(m);
		setType(ArenaTypes.PvP);
		lives = 10;
	}

	@Override
	public void Activate() {
		SetPlayers();
		Lives.clear();

		if (getArenaPlayers().size() > 1) {
			try {
				for (Player p : getArenaPlayers()) {
					if (!Lives.containsKey(p.getName())) {
						Lives.put(p.getName(), lives);
					}
				}

				GetSpawnArea();
				setArenaRunID(UUID.randomUUID());
				SetPlayersScoreBoards();

				setState(ArenaState.Active);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				getPlugin().writeToLog("Arena " + getName() + " failed to go to active state");
			}
		} else {
			Deactivate();
		}
	}

	public Scoreboard GetScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sb = manager.getNewScoreboard();
		Objective objective = sb.registerNewObjective("Lives Left", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Lives Left");

		if (Lives.keySet().size() > 0) {
			for (String s : Lives.keySet()) {
				Score score = objective.getScore(s + ": ");
				score.setScore(Lives.get(s));
			}
		}
		return sb;
	}

	private Location CanSpawnAt(Location currentLocation) {
		Block currentBlock = currentLocation.getBlock();
		Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
		Block blockAbove = currentBlock.getRelative(BlockFace.UP);

		if (currentBlock.getType() == Material.AIR && blockAbove.getType() == Material.AIR
				&& spawnMats.contains(blockBelow.getType())) {
			return currentLocation;
		} else {
			return null;
		}
	}

	public void GetSpawnArea() {
		Location spawnLocation;
		getSpawnArea().clear();

		List<Block> tempSpawnArea = Functions.getBlocksBetweenPoints(getSpawnArenaStartLocation(),
				getSpawnArenaEndLocation());

		if (tempSpawnArea != null && tempSpawnArea.size() > 0) {
			for (Block b : tempSpawnArea) {
				spawnLocation = b.getLocation();
				spawnLocation = CanSpawnAt(spawnLocation);
				if (spawnLocation != null && !b.equals(getSpawnArenaStartLocation())) {
					Block spawnBlock = spawnLocation.getBlock();
					getSpawnArea().add(spawnBlock);
				}
			}
		}
	}

	@Override
	public void Deactivate() {
		try {
			ClearingArena();
			setState(ArenaState.Deactivated);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog(
					"Arena " + getName() + " failed to go to " + ArenaState.Deactivated.toString() + "  state");
		}
	}

	public void PlayerKilled(Player p) {
		String heighestKills = "";
		String key = "";

		key = p.getName();

		int newLives = Lives.get(key) - 1;
		Lives.put(key, newLives);

		SetPlayersScoreBoards();

		for (String s : Lives.keySet()) {
			if (Lives.get(s) == 0) {
				new MessageTimer(getArenaPlayers(), "Arena " + ArenaName() + " has ended!!").run();
				Deactivate();
			}

			if (Lives.get(heighestKills) == null) {
				heighestKills = s;
			} else if (Lives.get(heighestKills) < Lives.get(s)) {
				heighestKills = s;
			}
		}
	}

	public void SetPlayersScoreBoards() {
		for (Player p : getArenaPlayers()) {
			if (!Lives.containsKey(p.getName())) {
				Lives.put(p.getName(), lives);
			}
			p.setScoreboard(GetScoreboard());
		}
	}

	@Override
	public void ProgressingWave() {
		if (getArenaPlayers().size() > 0) {
			setCurrentRunTimes(getCurrentRunTimes() + 1);

			if (getCurrentRunTimes() == 1) {
				new MessageTimer(getArenaPlayers(), ArenaName() + " has Started!!").run();
			}
			new PvPArenaTimer(this).runTaskLater(getPlugin(), getTimerPeriod());
		} else {
			Deactivate();
		}
	}

	@Override
	public void ClearingArena() {
		try {
			setState(ArenaState.ClearingArena);
			setArenaRunID(null);
			RestoreArena();

			ListIterator<Player> players = getArenaPlayers().listIterator();

			while (players.hasNext()) {
				Player p = players.next();
				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog(
					"Arena " + getName() + " failed to go to " + ArenaState.ClearingArena.toString() + "  state");
		}
	}

	@Override
	public void Phased() {
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
			getPlugin().writeToLog(
					"Arena " + getName() + " failed to go to " + ArenaState.ProgressingWave.toString() + "  state");
		}
	}

	@Override
	public void PhaseChanged(Phase p) {
		if (p.isActive()) {
			while (players.hasNext()) {
				Player pl = players.next();
				pl.setScoreboard(p.GetPhaseScoreboard());
			}
		} else if (p.isCompleted() && canTransitionToState(ArenaState.ProgressingWave)) {
			ProgressingWave();
		}
	}
}
