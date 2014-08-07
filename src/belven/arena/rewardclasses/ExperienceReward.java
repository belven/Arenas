package belven.arena.rewardclasses;

public class ExperienceReward extends Reward
{
    public int experience = 0;

    public ExperienceReward(int exp)
    {
        rewardType = RewardType.Experience;
        experience = exp;
    }
}
