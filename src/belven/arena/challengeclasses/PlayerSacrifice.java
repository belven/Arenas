package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import belven.arena.events.ChallengeComplete;

public class PlayerSacrifice extends Challenge {
	public List<Player> playersSacrificed = new ArrayList<Player>();
	public List<Player> players = new ArrayList<Player>();

	public PlayerSacrifice(ChallengeBlock cb, List<Player> players) {
		super(cb);
		type = ChallengeTypes.PlayerSacrifice;
		this.players = players;
	}

	@Override
	public boolean ChallengeComplete() {
		return playersSacrificed.size() == players.size();
	}

	@Override
	public Scoreboard SetChallengeScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sb = manager.getNewScoreboard();

		if (!ChallengeComplete()) {
			Objective objective = sb.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Sacrifice Challenge");
			Score score = objective.getScore("Amount Left: ");
			score.setScore(players.size());
		}

		return sb;
	}

	@Override
	public void interactedWith(Player p) {
		if (!playersSacrificed.contains(p)) {
			playersSacrificed.add(p);

			if (ChallengeComplete()) {
				Bukkit.getPluginManager().callEvent(new ChallengeComplete(this));
			}
			p.setHealth(0.0);
		} else {
			p.sendMessage("You cannot sacrifice yourself again");
		}
		return;
	}

	@Override
	public void EntityKilled(EntityType entityType) {
		// TODO Auto-generated method stub

	}

}
