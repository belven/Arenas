package belven.arena.challengeclasses;

import org.bukkit.scoreboard.Scoreboard;

public class ItemSacrifice extends ChallengeType {
	public ItemSacrifice() {
		type = ChallengeTypes.ItemSacrifice;
	}

	@Override
	public boolean ChallengeComplete() {
		return false;
	}

	@Override
	public Scoreboard SetChallengeScoreboard(ChallengeType ct) {
		// TODO Auto-generated method stub
		return null;
	}
}
