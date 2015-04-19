package belven.arena.rewardclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public static List<Item> RandomItemRewards(int amount) {
		List<Item> items = new ArrayList<Item>();

		for (Material m : ArenaManager.getItemMaterials()) {
			ChanceLevel cl = ArenaManager.getMaterialChance(m);
			amount = amount - cl.ordinal();
			if (amount <= 0) {
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
		int amountOfPlayers = players.size();

		Collections.sort(rewards, new Comparator<Item>() {
			@Override
			public int compare(Item i1, Item i2) {
				if (i1.getItemChance().ordinal() > i2.getItemChance().ordinal()) {
					return 1;
				} else if (i1.getItemChance().ordinal() < i2.getItemChance().ordinal()) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		int count = 0;

		for (Item i : rewards) {
			if (i.ShouldGive(players.size())) {
				count++;
				int amountToGive = amountOfPlayers * 10 / i.getItemChance().ordinal();
				i.getItem().setAmount(amountToGive);

				messtext += i.getType().name() + " " + String.valueOf(i.getAmount() + " ");

				for (Player p : players) {
					p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
					p.getInventory().addItem(i.getItem());
				}
			}

			if (count >= 4) {
				break;
			}
		}

		new MessageTimer(players, messtext + rewardType.name()).run();
	}
}
