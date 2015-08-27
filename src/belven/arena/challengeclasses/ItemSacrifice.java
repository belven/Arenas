package belven.arena.challengeclasses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class ItemSacrifice extends Challenge {
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

		return null;
	}

	@Override
	public void interactedWith(Player p) {

		return;
	}

	@Override
	public void EntityKilled(EntityType entityType) {

	}

}
