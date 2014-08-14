package belven.arena.rewardclasses;

public class ExperienceReward extends Reward {
	public double experience = 0;

	public ExperienceReward(double exp) {
		rewardType = RewardType.Experience;
		experience = exp;
	}
}
