package belven.arena.rewardclasses;

import java.util.Random;

import org.bukkit.entity.EntityType;

public abstract class Reward
{
    public enum RewardType
    {
        Default, Items, Experience, Boss
    }

    public static Reward GetRandomReward()
    {
        int ran = new Random().nextInt(2);
        switch (ran)
        {
        case 0:
            return new ExperienceReward(new Random().nextInt(20) + 1);
        case 1:
            return new ItemReward(ItemReward.RandomItemRewards());
        case 2:
            return new BossReward(EntityType.ZOMBIE);
        default:
            return new ExperienceReward(new Random().nextInt(10) + 1);
        }
    }

    public RewardType rewardType = RewardType.Default;
}
