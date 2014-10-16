package belven.arena.arenas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.timedevents.MessageTimer;
import belven.arena.timedevents.PvPArenaTimer;
import belven.teams.TeamManager;

public class PvPArena extends BaseArena {

	public HashMap<String, Integer> Lives = new HashMap<String, Integer>();
	public TeamManager tm;
	public List<Material> spawnMats = new ArrayList<Material>();
	public int lives = 0;

	public PvPArena(Location startLocation, Location endLocation, String ArenaName, int Radius, ArenaManager Plugin,
			Material m, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Radius, Plugin, TimerPeriod);
		tm = plugin.teams;
		spawnMats.add(m);
		type = ArenaTypes.PvP;
		lives = 10;
	}

	@Override
	public void Activate() {
		SetPlayers();
		Lives.clear();
		if (arenaPlayers.size() != 0) {
			for (Player p : arenaPlayers) {
				if (tm != null && tm.isInATeam(p) && !Lives.containsKey(tm.getTeam(p).teamName)) {
					Lives.put(tm.getTeam(p).teamName, lives);
				} else if (!Lives.containsKey(p.getName())) {
					Lives.put(p.getName(), lives);
				}
			}

			GetSpawnArea();
			arenaRunID = UUID.randomUUID();
			isActive = true;
			SetPlayersScoreBoards();
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
		spawnArea.clear();

		List<Block> tempSpawnArea = Functions.getBlocksBetweenPoints(spawnArenaStartLocation, spawnArenaEndLocation);

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
	public void Deactivate() {
		arenaRunID = null;
		RestoreArena();
		isActive = false;
		for (Player p : arenaPlayers) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	public void PlayerKilled(Player p) {
		String heighestKills = "";
		String key = "";

		if (tm != null && tm.isInATeam(p)) {
			key = tm.getTeam(p).teamName;
		} else {
			key = p.getName();
		}

		int newLives = Lives.get(key) - 1;
		Lives.put(key, newLives);

		SetPlayersScoreBoards();

		for (String s : Lives.keySet()) {
			if (Lives.get(s) == 0) {
				new MessageTimer(arenaPlayers, "Arena " + ArenaName() + " has ended!!").run();
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
		for (Player p : arenaPlayers) {
			p.setScoreboard(GetScoreboard());
		}
	}

	@Override
	public void GoToNextWave() {
		if (arenaPlayers.size() > 0) {
			currentRunTimes++;

			if (currentRunTimes == 1) {
				new MessageTimer(arenaPlayers, ArenaName() + " has Started!!").run();
			}
			new PvPArenaTimer(this).runTaskLater(plugin, timerPeriod);
		} else {
			Deactivate();
		}
	}
}