package belven.arena.rewardclasses;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import belven.arena.challengeclasses.ChallengeBlock;
import belven.resources.Functions;

public abstract class Reward {
	public enum RewardType {
		Default, Items, Experience, Boss
	}

	private final static List<Double> expRewards = Arrays.asList(0.5, 0.6, 0.7, 0.8, 0.9, 1.0);

	public static Reward GetRandomReward() {
		int ran = new Random().nextInt(5);
		double exp = getExpRewards().get(Functions.getRandomIndex(getExpRewards()));

		switch (ran) {
		case 0:
			return new ExperienceReward(exp);
		case 1:
			return new ItemReward(ItemReward.RandomItemRewards(new Random().nextInt(20)));
		case 2:
		case 3:
			return new BossReward(EntityType.ZOMBIE);
		default:
			return new ExperienceReward(exp);
		}
	}

	public RewardType rewardType = RewardType.Default;

	public abstract void GiveRewards(ChallengeBlock cb, List<Player> players);

	public static List<Double> getExpRewards() {
		return expRewards;
	}
}
