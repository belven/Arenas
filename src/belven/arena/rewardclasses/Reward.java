package belven.arena.rewardclasses;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.EntityType;

public abstract class Reward
{
    public enum RewardType
    {
        Default, Items, Experience, Boss
    }

    private final static List<Double> expRewards = Arrays.asList(0.1, 0.2, 0.3,
            0.5, 0.6);

    public static Reward GetRandomReward()
    {
        int ran = new Random().nextInt(2);

        double exp = expRewards.get(new Random().nextInt(expRewards.size()));

        switch (ran)
        {
        case 0:
            return new ExperienceReward(exp);
        case 1:
            return new ItemReward(ItemReward.RandomItemRewards());
        case 2:
            return new BossReward(EntityType.ZOMBIE);
        default:
            return new ExperienceReward(exp);
        }
    }

    public RewardType rewardType = RewardType.Default;
}
