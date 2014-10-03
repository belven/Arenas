package belven.arena.rewardclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import belven.arena.ArenaManager;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.rewardclasses.Item.ChanceLevel;
import belven.arena.timedevents.MessageTimer;

public class ItemReward extends Reward {
	public static List<Item> RandomItemRewards() {
		List<Item> items = new ArrayList<Item>();
		int amount = 10;

		for (Material m : ArenaManager.getItemMaterials()) {
			ChanceLevel cl = ArenaManager.getMaterialChance(m);
			amount = amount - cl.ordinal();
			if (amount >= 0) {
				amount = 1;
			}
			items.add(new Item(m, amount, cl));
			amount = 10;
		}
		return items;
	}

	public List<Item> rewards = new ArrayList<Item>();

	public ItemReward(List<Item> items) {
		rewardType = RewardType.Items;
		rewards = items;
	}

	public List<ItemStack> GetItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (Item i : rewards) {
			items.add(i.getItem());
		}

		return items;
	}

	@Override
	public void GiveRewards(ChallengeBlock cb, List<Player> players) {
		String messtext = "Challenge has been completed you get ";

		for (Player p : players) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

			List<Item> items = rewards;

			for (Item i : items) {
				if (i.ShouldGive(players.size())) {
					messtext += i.getType().name() + " "
							+ String.valueOf(i.getAmount() + " ");
					p.getInventory().addItem(i.getItem());
				}
			}
		}

		new MessageTimer(players, messtext + rewardType.name()).run();
	}
}
