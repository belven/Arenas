package belven.arena.arenas;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import resources.Functions;
import resources.Group;
import belven.arena.ArenaManager;
import belven.arena.arenas.BaseArena.ArenaTypes;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.resources.SavedBlock;

public class BaseArenaData extends Group {
	protected ArenaManager plugin;
	protected boolean isActive = false;
	protected ArenaTypes type;
	protected String name = "";
	protected Block blockToActivate;
	protected Block deactivateBlock;
	protected Block arenaWarp;
	protected ChallengeBlock currentChallengeBlock;
	protected int ChallengeBlockWave;
	protected List<Block> arenaArea = new ArrayList<Block>();
	protected List<SavedBlock> originalBlocks = new ArrayList<SavedBlock>();
	protected List<Player> arenaPlayers = new ArrayList<Player>();
	protected List<Block> spawnArea = new ArrayList<Block>();
	protected List<BaseArena> linkedArenas = new ArrayList<BaseArena>();
	protected Location spawnArenaStartLocation;
	protected Location spawnArenaEndLocation;
	protected Location ArenaStartLocation;
	protected Location ArenaEndLocation;
	protected Location arenaChest;
	protected int maxRunTimes = 0;
	protected int timerPeriod = 0;
	protected int eliteWave = 0;
	protected int averageLevel = 0;
	protected int maxMobCounter = 0;
	protected int linkedArenaDelay = 0;
	protected int currentRunTimes = 0;
	protected List<ItemStack> arenaRewards = new ArrayList<ItemStack>();
	protected UUID arenaRunID;

	public BaseArenaData(Location startLocation, Location endLocation, String ArenaName, ArenaManager Plugin,
			int TimerPeriod) {
		super(new ArrayList<Player>(), ArenaName);
		setSpawnArenaStartLocation(startLocation);
		setSpawnArenaEndLocation(endLocation);
		setArenaStartLocation(startLocation);
		setArenaEndLocation(endLocation);

		setBlockToActivate(startLocation.getBlock());
		setDeactivateBlock(Functions.offsetLocation(startLocation, 0, 2, 0).getBlock());

		setArenaWarp(startLocation.getBlock());
		setTimerPeriod(TimerPeriod);
		setName(ArenaName);
		setPlugin(Plugin);
		setMaxRunTimes(5);
	}

	// public BaseArenaData(boolean isActive, ChallengeBlock
	// currentChallengeBlock, int challengeBlockWave,
	// List<Block> arenaArea, List<SavedBlock> originalBlocks, List<Player>
	// arenaPlayers, List<Block> spawnArea,
	// List<BaseArena> linkedArenas, int currentRunTimes, List<ItemStack>
	// arenaRewards, UUID arenaRunID) {
	// this.isActive = isActive;
	// this.currentChallengeBlock = currentChallengeBlock;
	// ChallengeBlockWave = challengeBlockWave;
	// this.arenaArea = arenaArea;
	// this.originalBlocks = originalBlocks;
	// this.arenaPlayers = arenaPlayers;
	// this.spawnArea = spawnArea;
	// this.linkedArenas = linkedArenas;
	// this.currentRunTimes = currentRunTimes;
	// this.arenaRewards = arenaRewards;
	// this.arenaRunID = arenaRunID;
	// }

	// public BaseArenaData(BaseArenaData bad) {
	// setValues(bad, this);
	// }

	public void setValues(Object exampleClassWithValues, Object exampleClassToGetValues) {
		try {
			Class<?> classA = exampleClassToGetValues.getClass();
			Class<?> classB = exampleClassWithValues.getClass();

			// Loop through each field on classA
			for (Field field : classA.getDeclaredFields()) {
				Field valueField = classB.getField(field.getName());

				// See if classB has the same Field
				if (valueField != null) {

					// Set classA field value to the value of classBs' field
					field.set(exampleClassToGetValues, valueField.get(exampleClassWithValues));
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public ArenaManager getPlugin() {
		return plugin;
	}

	public void setPlugin(ArenaManager plugin) {
		this.plugin = plugin;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public ArenaTypes getType() {
		return type;
	}

	public void setType(ArenaTypes type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public Block getBlockToActivate() {
		return blockToActivate;
	}

	public void setBlockToActivate(Block blockToActivate) {
		this.blockToActivate = blockToActivate;
	}

	public Block getDeactivateBlock() {
		return deactivateBlock;
	}

	public void setDeactivateBlock(Block deactivateBlock) {
		this.deactivateBlock = deactivateBlock;
	}

	public Block getArenaWarp() {
		return arenaWarp;
	}

	public void setArenaWarp(Block arenaWarp) {
		this.arenaWarp = arenaWarp;
	}

	public ChallengeBlock getCurrentChallengeBlock() {
		return currentChallengeBlock;
	}

	public void setCurrentChallengeBlock(ChallengeBlock currentChallengeBlock) {
		this.currentChallengeBlock = currentChallengeBlock;
	}

	public int getChallengeBlockWave() {
		return ChallengeBlockWave;
	}

	public void setChallengeBlockWave(int challengeBlockWave) {
		ChallengeBlockWave = challengeBlockWave;
	}

	public List<Block> getArenaArea() {
		return arenaArea;
	}

	public void setArenaArea(List<Block> arenaArea) {
		this.arenaArea = arenaArea;
	}

	public List<SavedBlock> getOriginalBlocks() {
		return originalBlocks;
	}

	public void setOriginalBlocks(List<SavedBlock> originalBlocks) {
		this.originalBlocks = originalBlocks;
	}

	public List<Player> getArenaPlayers() {
		return arenaPlayers;
	}

	public void setArenaPlayers(List<Player> arenaPlayers) {
		this.arenaPlayers = arenaPlayers;
	}

	public List<Block> getSpawnArea() {
		return spawnArea;
	}

	public void setSpawnArea(List<Block> spawnArea) {
		this.spawnArea = spawnArea;
	}

	public List<BaseArena> getLinkedArenas() {
		return linkedArenas;
	}

	public void setLinkedArenas(List<BaseArena> linkedArenas) {
		this.linkedArenas = linkedArenas;
	}

	public Location getSpawnArenaStartLocation() {
		return spawnArenaStartLocation;
	}

	public void setSpawnArenaStartLocation(Location spawnArenaStartLocation) {
		this.spawnArenaStartLocation = spawnArenaStartLocation;
	}

	public Location getSpawnArenaEndLocation() {
		return spawnArenaEndLocation;
	}

	public void setSpawnArenaEndLocation(Location spawnArenaEndLocation) {
		this.spawnArenaEndLocation = spawnArenaEndLocation;
	}

	public Location getArenaStartLocation() {
		return ArenaStartLocation;
	}

	public void setArenaStartLocation(Location arenaStartLocation) {
		ArenaStartLocation = arenaStartLocation;
	}

	public Location getArenaEndLocation() {
		return ArenaEndLocation;
	}

	public void setArenaEndLocation(Location arenaEndLocation) {
		ArenaEndLocation = arenaEndLocation;
	}

	public int getMaxRunTimes() {
		return maxRunTimes;
	}

	public void setMaxRunTimes(int maxRunTimes) {
		this.maxRunTimes = maxRunTimes;
	}

	public int getTimerPeriod() {
		return timerPeriod;
	}

	public void setTimerPeriod(int timerPeriod) {
		this.timerPeriod = timerPeriod;
	}

	public int getEliteWave() {
		return eliteWave;
	}

	public void setEliteWave(int eliteWave) {
		this.eliteWave = eliteWave;
	}

	public int getAverageLevel() {
		return averageLevel;
	}

	public void setAverageLevel(int averageLevel) {
		this.averageLevel = averageLevel;
	}

	public int getMaxMobCounter() {
		return maxMobCounter;
	}

	public void setMaxMobCounter(int maxMobCounter) {
		this.maxMobCounter = maxMobCounter;
	}

	public int getLinkedArenaDelay() {
		return linkedArenaDelay;
	}

	public void setLinkedArenaDelay(int linkedArenaDelay) {
		this.linkedArenaDelay = linkedArenaDelay;
	}

	public int getCurrentRunTimes() {
		return currentRunTimes;
	}

	public void setCurrentRunTimes(int currentRunTimes) {
		this.currentRunTimes = currentRunTimes;
	}

	public List<ItemStack> getArenaRewards() {
		List<ItemStack> rewards = new ArrayList<>();

		if (getArenaChest() != null) {
			BlockState state = getArenaChest().getBlock().getState();

			if (state instanceof Chest) {
				ItemStack[] items = ((Chest) state).getInventory().getContents();
				Collections.addAll(rewards, items);
				return rewards;
			}
		}
		rewards = arenaRewards;

		return rewards;
	}

	public void setArenaRewards(List<ItemStack> arenaRewards) {
		this.arenaRewards = arenaRewards;
	}

	public UUID getArenaRunID() {
		return arenaRunID;
	}

	public void setArenaRunID(UUID arenaRunID) {
		this.arenaRunID = arenaRunID;
	}

	public Location getArenaChest() {
		return arenaChest;
	}

	public void setArenaChest(Location arenaChest) {
		this.arenaChest = arenaChest;
	}
}