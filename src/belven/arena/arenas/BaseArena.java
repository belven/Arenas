package belven.arena.arenas;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.phases.Phase;
import belven.arena.resources.SavedBlock;
import belven.arena.rewardclasses.Item;
import belven.arena.rewardclasses.ItemReward;
import belven.resources.Functions;

/**
 * @author sam
 * 
 */
public abstract class BaseArena extends BaseArenaData implements Phaseable {

	public BaseArena(Location startLocation, Location endLocation, String ArenaName, ArenaManager Plugin,
			int TimerPeriod) {

		super(startLocation, endLocation, ArenaName, Plugin, TimerPeriod);
		getPlugin().currentArenaBlocks.add(this);
	}

	public String ArenaName() {
		return ChatColor.RED + getName() + ChatColor.WHITE;
	}

	public void ClearPlayerScoreboards() {
		ListIterator<Player> players = getArenaPlayers().listIterator();

		while (players.hasNext()) {
			clearPlayerScoreboard(players.next());
		}
	}

	public void clearPlayerScoreboard(Player p) {
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public abstract void Activate();

	public abstract void Deactivate();

	public abstract void ClearingArena();

	public abstract void Phased();

	public boolean shouldPhase() {
		return getPhases().containsKey(getCurrentRunTimes());
	}

	public void RestoreArena() {
		for (SavedBlock sb : getOriginalBlocks()) {
			sb.bs.update(true);
			sb.bs.removeMetadata("ArenaAreaBlock", getPlugin());
		}
	}

	/**
	 * Creates an amount of Phases for random waves, based on a percent of the waves i.e 0.3 will cause 30% of the waves to have a phase,
	 * i.e. there will be 3 phases with 10 waves
	 * 
	 * @param percent - the percent of waves that will create phases
	 */
	public void GenerateRandomPhases(double percent) {
		if (getMaxRunTimes() < 0) {
			return;
		}

		int amount = (int) Math.round(getMaxRunTimes() * percent);

		if (amount <= 0) {
			amount = 1;
		}

		for (int count = 0; count < amount; count = getPhases().size()) {
			// Get a random wave to create a Phase
			int wave = new Random().nextInt(getMaxRunTimes());
			if (wave <= 0) {
				amount = 1;
			}
			// Don't add 2 phases to the same wave
			if (!getPhases().containsKey(wave)) {
				getPhases().put(wave, Phase.getRandomPhase(getPlugin(), this, getSpawnArea()));
			}
		}
	}

	public synchronized void GiveRewards() {
		try {
			setState(ArenaState.GivingRewards);

			int count = getArenaPlayers().size();
			Iterator<Player> ArenaPlayers = getArenaPlayers().listIterator();

			while (ArenaPlayers.hasNext()) {
				Player p = ArenaPlayers.next();
				if (getArenaRewards().size() <= 0) {
					ItemReward ir = new ItemReward(ItemReward.RandomItemRewards(getSpawnArea().size()));
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
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog(
					"Arena " + getName() + " failed to go to " + ArenaState.GivingRewards.toString() + " state");
		}
	}

	public abstract void ProgressingWave();

	/**
	 * Pre-generates the arenas blocks to determine if a player is standing on one
	 */
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

	public static Block GetRandomArenaSpawnBlock(BaseArena ab) {
		return ab.getSpawnArea().get(Functions.getRandomIndex(ab.getSpawnArea()));
	}

	public static Location GetRandomArenaSpawnLocation(BaseArena ab) {
		return Functions.offsetLocation(GetRandomArenaSpawnBlock(ab).getLocation(), 0.5, 0, 0.5);
	}
}
