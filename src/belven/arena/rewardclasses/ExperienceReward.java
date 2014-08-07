package belven.arena.rewardclasses;

public class ExperienceReward extends Reward
{
    int experience = 0;

    public ExperienceReward(int exp)
    {
        rewardType = Type.Experience;
        experience = exp;
    }
}
