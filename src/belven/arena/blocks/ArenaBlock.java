package belven.arena.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import resources.EntityFunctions;
import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.resources.SavedBlock;
import belven.arena.rewardclasses.Item;
import belven.arena.rewardclasses.ItemReward;

public abstract class ArenaBlock {
	public enum ArenaTypes {
		Standard, PvP, Temp
	}

	public ArenaManager plugin;
	public boolean isActive = false;
	public ArenaTypes type;

	public String name;
	public Block blockToActivate, deactivateBlock, arenaWarp;
	public ChallengeBlock currentChallengeBlock = null;
	public int ChallengeBlockWave = 1;

	public List<Block> arenaArea = new ArrayList<Block>();
	public List<SavedBlock> originalBlocks = new ArrayList<SavedBlock>();
	public List<Player> arenaPlayers = new ArrayList<Player>();
	public List<Block> spawnArea = new ArrayList<Block>();
	public List<ArenaBlock> linkedArenas = new ArrayList<ArenaBlock>();

	public Location LocationToCheckForPlayers, spawnArenaStartLocation,
			spawnArenaEndLocation, ArenaStartLocation, ArenaEndLocation;

	public int radius, maxRunTimes, timerPeriod, eliteWave, averageLevel,
			maxMobCounter, linkedArenaDelay, currentRunTimes = 0;

	public List<ItemStack> arenaRewards = new ArrayList<ItemStack>();
	public UUID arenaRunID = null;

	public ArenaBlock(Location startLocation, Location endLocation,
			String ArenaName, int Radius, ArenaManager Plugin, int TimerPeriod) {

		spawnArenaStartLocation = startLocation;
		spawnArenaEndLocation = endLocation;
		ArenaStartLocation = startLocation;
		ArenaEndLocation = endLocation;

		blockToActivate = startLocation.getBlock();
		deactivateBlock = Functions.offsetLocation(startLocation, 0, 2, 0)
				.getBlock();

		LocationToCheckForPlayers = blockToActivate.getLocation();
		arenaWarp = startLocation.getBlock();
		radius = Radius;
		timerPeriod = TimerPeriod;
		name = ArenaName;
		plugin = Plugin;
		maxRunTimes = 5;
		plugin.currentArenaBlocks.add(this);
	}

	public String ArenaName() {
		return ChatColor.RED + name + ChatColor.WHITE;
	}

	public abstract void Activate();

	public abstract void Deactivate();

	public void RestoreArena() {
		for (SavedBlock sb : originalBlocks) {
			sb.bs.update(true);
			sb.bs.removeMetadata("ArenaAreaBlock", plugin);
		}
	}

	public void GiveRewards() {
		int count = arenaPlayers.size();
		Iterator<Player> ArenaPlayers = arenaPlayers.iterator();

		while (ArenaPlayers.hasNext()) {
			Player p = ArenaPlayers.next();
			if (arenaRewards.size() <= 0) {
				ItemReward ir = new ItemReward(ItemReward.RandomItemRewards());
				for (Item i : ir.rewards) {
					if (i.ShouldGive(count)) {
						p.getInventory().addItem(i.getItem());
					}
				}
			}

			for (ItemStack is : arenaRewards) {
				if (is != null) {
					p.getInventory().addItem(is);
				}
			}
		}
	}

	public abstract void GoToNextWave();

	public void SetPlayers() {
		Player[] tempPlayers = EntityFunctions.getNearbyPlayersNew(
				LocationToCheckForPlayers, radius);
		if (tempPlayers.length > 0) {
			GetArenaArea();
			for (Player p : tempPlayers) {
				Block b = p.getLocation().getBlock();

				if (!plugin.IsPlayerInArena(p)
						&& b.hasMetadata("ArenaAreaBlock")) {
					MetadataValue data = b.getMetadata("ArenaAreaBlock").get(0);
					if (data.value() != null) {
						ArenaBlock ab = (ArenaBlock) data.value();
						if (data != null && ab == this) {
							plugin.WarpToArena(p, this);
						}
					}
				}
			}
		}
	}

	public void GetArenaArea() {
		arenaArea = Functions.getBlocksBetweenPoints(ArenaStartLocation,
				ArenaEndLocation);

		originalBlocks.clear();

		for (Block b : arenaArea) {
			originalBlocks.add(new SavedBlock(b));
			b.setMetadata("ArenaAreaBlock",
					new FixedMetadataValue(plugin, this));
		}
	}

	public void GetPlayersAverageLevel() {
		if (arenaPlayers.size() == 0) {
			return;
		}

		int totalLevels = 0;
		averageLevel = 0;
		maxMobCounter = 0;

		for (Player p : arenaPlayers) {
			totalLevels += p.getLevel();
		}

		if (totalLevels == 0) {
			totalLevels = 1;
		}

		averageLevel = (int) (totalLevels / arenaPlayers.size());
		maxMobCounter = (int) (totalLevels / arenaPlayers.size())
				+ (arenaPlayers.size() * 5);

		if (maxMobCounter > (arenaPlayers.size() * 15)) {
			maxMobCounter = arenaPlayers.size() * 15;
		}
	}

	public static Block GetRandomArenaSpawnBlock(ArenaBlock ab) {
		Block b;
		b = ab.spawnArea.get(new Random().nextInt(ab.spawnArea.size()));
		return b;
	}

	public static Location GetRandomArenaSpawnLocation(ArenaBlock ab) {
		return Functions.offsetLocation(GetRandomArenaSpawnBlock(ab)
				.getLocation(), 0.5, 0, 0.5);
	}
}