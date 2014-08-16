package belven.arena.rewardclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class ItemReward extends Reward {
	public List<Item> rewards = new ArrayList<Item>();

	public ItemReward(List<Item> items) {
		rewardType = RewardType.Items;
		rewards = items;
	}

	public static List<Item> RandomItemRewards() {
		List<Item> items = new ArrayList<Item>();
		items.add(new Item(Material.DIAMOND));
		return items;
	}
}
