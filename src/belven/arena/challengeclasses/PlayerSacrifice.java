package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import belven.arena.events.ChallengeComplete;

public class PlayerSacrifice extends ChallengeType {
	public List<Player> playersSacrificed = new ArrayList<Player>();
	public int amountToSacrifice = 1;

	public PlayerSacrifice(int amount) {
		type = ChallengeTypes.PlayerSacrifice;
		amountToSacrifice = amount;
	}

	@Override
	public boolean ChallengeComplete() {
		return amountToSacrifice <= 0;
	}

	public void SacrificePlayer(Player p) {
		if (!playersSacrificed.contains(p)) {
			playersSacrificed.add(p);
			amountToSacrifice--;

			if (ChallengeComplete()) {
				Bukkit.getPluginManager().callEvent(new ChallengeComplete(this));
			}
			p.setHealth(0.0);
		} else {
			p.sendMessage("You cannot sacrifice yourself again");
		}
	}

	@Override
	public Scoreboard SetChallengeScoreboard(ChallengeType ct) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sb = manager.getNewScoreboard();

		if (!ChallengeComplete()) {
			Objective objective = sb.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Sacrifice Challenge");
			Score score = objective.getScore("Amount Left: ");
			score.setScore(amountToSacrifice);
		}

		return sb;
	}
}
