package belven.arena.rewardclasses;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.timedevents.MessageTimer;

public class ExperienceReward extends Reward {
	public double experience = 0;

	public ExperienceReward(double exp) {
		rewardType = RewardType.Experience;
		experience = exp;
	}

	@Override
	public void GiveRewards(ChallengeBlock cb, List<Player> players) {

		String messtext = "Challenge has been completed you get ";

		for (Player p : players) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

			int expToGive = (int) (p.getExpToLevel() * experience);
			p.giveExp(expToGive);

			messtext += String.valueOf(expToGive) + " ";

		}

		new MessageTimer(players, messtext + rewardType.name()).run();

	}
}
