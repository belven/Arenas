package belven.arena.rewardclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import belven.arena.ArenaManager;
import belven.arena.rewardclasses.Item.ChanceLevel;

public class ItemReward extends Reward {
	public List<Item> rewards = new ArrayList<Item>();

	public ItemReward(List<Item> items) {
		rewardType = RewardType.Items;
		rewards = items;
	}

	public static List<Item> RandomItemRewards() {
		List<Item> items = new ArrayList<Item>();
		int amount = 10;

		for (Material m : ArenaManager.getItemMaterials()) {
			ChanceLevel cl = ArenaManager.getMaterialChance(m);
			amount = amount - cl.ordinal();
			if (amount >= 0)
				amount = 1;
			items.add(new Item(m, amount, cl));
			amount = 10;
		}
		return items;
	}
}
