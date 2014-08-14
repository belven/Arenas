package belven.arena.rewardclasses;

import org.bukkit.entity.EntityType;

public class BossReward extends Reward {
	public EntityType boss;

	public BossReward(EntityType et) {
		rewardType = RewardType.Boss;
		boss = et;
	}
}
