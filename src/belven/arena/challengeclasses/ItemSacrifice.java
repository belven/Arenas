package belven.arena.challengeclasses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class ItemSacrifice extends ChallengeType {
	public ItemSacrifice(ChallengeBlock cb) {
		super(cb);
		type = ChallengeTypes.ItemSacrifice;
	}

	@Override
	public boolean ChallengeComplete() {
		return false;
	}

	@Override
	public Scoreboard SetChallengeScoreboard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean ChallengeBlockInteracted(Player p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void EntityKilled(EntityType entityType) {
		// TODO Auto-generated method stub

	}

}
