package belven.arena.arenas;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.resources.SavedBlock;
import belven.arena.rewardclasses.Item;
import belven.arena.rewardclasses.ItemReward;

public abstract class BaseArena extends BaseArenaData {
	public enum ArenaTypes {
		Standard, PvP, Temp
	}

	public BaseArena(Location startLocation, Location endLocation, String ArenaName, ArenaManager Plugin,
			int TimerPeriod) {

		super(startLocation, endLocation, ArenaName, Plugin, TimerPeriod);
		getPlugin().currentArenaBlocks.add(this);
	}

	public String ArenaName() {
		return ChatColor.RED + getName() + ChatColor.WHITE;
	}

	public abstract void Activate();

	public abstract void Deactivate();

	public void RestoreArena() {
		for (SavedBlock sb : getOriginalBlocks()) {
			sb.bs.update(true);
			sb.bs.removeMetadata("ArenaAreaBlock", getPlugin());
		}
	}

	public void GiveRewards() {
		int count = getArenaPlayers().size();
		Iterator<Player> ArenaPlayers = getArenaPlayers().listIterator();

		while (ArenaPlayers.hasNext()) {
			Player p = ArenaPlayers.next();
			if (getArenaRewards().size() <= 0) {
				ItemReward ir = new ItemReward(ItemReward.RandomItemRewards());
				for (Item i : ir.rewards) {
					if (i.ShouldGive(count)) {
						p.getInventory().addItem(i.getItem());
					}
				}
			}

			p.sendMessage("Giving rewards");
			for (ItemStack is : getArenaRewards()) {
				if (is != null) {
					p.getInventory().addItem(is);
				}
			}

			p.removeMetadata("InArena", plugin);
		}
	}

	public abstract void GoToNextWave();

	public void SetPlayers() {
		GetArenaArea();

		for (Player p : getPlugin().onlinePlayers) {
			Block b = p.getLocation().getBlock();
			List<MetadataValue> blockData = MDM.getMetaData(MDM.ArenaAreaBlock, b);

			if (!getPlugin().IsPlayerInArena(p) && blockData != null) {

				MetadataValue data = blockData.get(0);

				if (data.value() != null) {
					BaseArena ab = (BaseArena) data.value();
					if (data != null && ab == this) {
						ab.getArenaPlayers().add(p);
						getPlugin().PlayersInArenas.put(p, this);
						getPlugin().setPlayerMetaData(this);
					}
				}
			}
		}

		setPlayers(getArenaPlayers());
	}

	public void GetArenaArea() {
		setArenaArea(Functions.getBlocksBetweenPoints(getArenaStartLocation(), getArenaEndLocation()));

		getOriginalBlocks().clear();

		for (Block b : getArenaArea()) {
			getOriginalBlocks().add(new SavedBlock(b));
			b.setMetadata(MDM.ArenaAreaBlock, new FixedMetadataValue(getPlugin(), this));
		}
	}

	public void GetPlayersAverageLevel() {
		if (getArenaPlayers().size() == 0) {
			return;
		}

		int totalLevels = 0;
		setAverageLevel(0);
		setMaxMobCounter(0);

		for (Player p : getArenaPlayers()) {
			totalLevels += p.getLevel();
		}

		if (totalLevels == 0) {
			totalLevels = 1;
		}

		setAverageLevel(totalLevels / getArenaPlayers().size());
		setMaxMobCounter(totalLevels / getArenaPlayers().size() + getArenaPlayers().size() * 5);

		if (getMaxMobCounter() > getArenaPlayers().size() * 7) {
			setMaxMobCounter(getArenaPlayers().size() * 7);
		}
	}

	public static Block GetRandomArenaSpawnBlock(BaseArena ab) {
		return ab.getSpawnArea().get(new Random().nextInt(ab.getSpawnArea().size()));
	}

	public static Location GetRandomArenaSpawnLocation(BaseArena ab) {
		return Functions.offsetLocation(GetRandomArenaSpawnBlock(ab).getLocation(), 0.5, 0, 0.5);
	}
}