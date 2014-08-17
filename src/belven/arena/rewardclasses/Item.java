package belven.arena.rewardclasses;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import belven.arena.ArenaManager;

public class Item {
	public enum ChanceLevel {
		Always, VeryEasy, Easy, Medium, Hard, VeryHard
	}

	private ItemStack item = new ItemStack(Material.AIR);
	private ChanceLevel chance;

	public Item(ItemStack is, ChanceLevel cl) {
		item = is;
		chance = cl;
	}

	public Item(Material m, int amount, ChanceLevel cl) {
		item = new ItemStack(m, amount);
		chance = cl;
	}

	public Item(Material m, ChanceLevel cl) {
		this(m, 1, cl);
	}

	public Item(Material m) {
		this(m, 1, ArenaManager.getMaterialChance(m));
	}

	public Item(ItemStack is) {
		this(is, ChanceLevel.Always);
	}

	public int getAmount() {
		return item.getAmount();
	}

	public Material getType() {
		return item.getType();
	}

	public ItemStack getItem() {
		return item;
	}

	public ChanceLevel getItemChance() {
		return chance;
	}

	public boolean ShouldGive(ChanceLevel cl) {
		return getItemChance() == cl;
	}

	public boolean ShouldGive(int chance) {
		return getItemChance().ordinal() <= chance;
	}
}
