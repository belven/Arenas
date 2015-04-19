package belven.arena.arenas;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import belven.arena.ArenaManager;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.phases.Phase;
import belven.arena.resources.SavedBlock;
import belven.arena.timedevents.ArenaTimer;
import belven.resources.Functions;
import belven.resources.Group;

/**
 * @author sam
 * 
 */
public class BaseArenaData extends Group {
	public enum ArenaTypes {
		Standard, PvP, Temp
	}

	public enum ArenaState {
		Active, Deactivated, Phased, ProgressingWave, GivingRewards, ClearingArena
	}

	private ArenaState state = ArenaState.Deactivated;
	protected ArenaManager plugin;
	private ArenaTimer timer;
	protected ArenaTypes type;
	protected String name = "";
	protected Block blockToActivate, deactivateBlock, arenaWarp;
	protected ChallengeBlock currentChallengeBlock;
	private HashMap<Integer, Phase> phases = new HashMap<>();
	private Phase activePhase;
	protected List<Block> arenaArea = new ArrayList<Block>(), spawnArea = new ArrayList<Block>();
	protected List<SavedBlock> originalBlocks = new ArrayList<SavedBlock>();
	protected List<BaseArena> linkedArenas = new ArrayList<BaseArena>();
	protected Location spawnArenaStartLocation, spawnArenaEndLocation, ArenaStartLocation, ArenaEndLocation,
			arenaChest;

	protected int maxRunTimes = 0, timerPeriod = 0, averageLevel = 0, linkedArenaDelay = 0, currentRunTimes = 0,
			ChallengeBlockWave = 0;

	protected List<ItemStack> arenaRewards = new ArrayList<ItemStack>();
	protected UUID arenaRunID;
	protected List<UUID> editors = new ArrayList<>();

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
		return getState() == ArenaState.Active;
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
		return getPlayers();
	}

	public void setArenaPlayers(List<Player> arenaPlayers) {
		setPlayers(arenaPlayers);
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

	public int getAverageLevel() {
		return averageLevel;
	}

	public void setAverageLevel(int averageLevel) {
		this.averageLevel = averageLevel;
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

	public List<UUID> getEditors() {
		return editors != null ? editors : new ArrayList<UUID>();
	}

	public void setEditors(List<UUID> editors) {
		this.editors = editors;
	}

	public HashMap<Integer, Phase> getPhases() {
		return phases;
	}

	public void setPhases(HashMap<Integer, Phase> phases) {
		this.phases = phases;
	}

	public Phase getActivePhase() {
		return activePhase;
	}

	/**
	 * Sets the arenas active phase, activates the phase and removes it from the list.
	 * 
	 * @param activePhase - The new active phase
	 */
	public void setActivePhase(Phase activePhase) {
		this.activePhase = activePhase;
		if (activePhase != null) {
			activePhase.activate();
			getPhases().remove(activePhase);
		}
	}

	public ArenaState getState() {
		return state;
	}

	public void setState(ArenaState state) throws IllegalStateException {
		if (canTransitionToState(state)) {
			this.state = state;
			getPlugin().writeToLog("Arena " + getName() + " changed state to " + state.toString());
		} else {
			throw new IllegalStateException("Tried to trasition from state " + getState().toString() + " to state "
					+ state.toString());
		}
	}

	// Based on the arenas active state, this will say what state it can change to
	public boolean canTransitionToState(ArenaState state) {
		switch (getState()) {
		case Active: // Go to Deactivated because the arena couldn't start, go straight to phased to prevent a wave
						// go to the first wave or clear the arena as everyone left after it started
			return state == ArenaState.Deactivated || state == ArenaState.Phased || state == ArenaState.ProgressingWave
					|| state == ArenaState.ClearingArena || state == ArenaState.GivingRewards;
		case GivingRewards: // Ensure that the arena is only cleared once rewards are given
			return state == ArenaState.ClearingArena;
		case ClearingArena: // If we've cleared the arena then there's nothing left so deactive
			return state == ArenaState.Deactivated;
		case ProgressingWave: // We have spawned entities so the arena should still active
			return state == ArenaState.Active;
		case Phased: // A phase will stop new waves or arena completion so there is either a new wave or the arena is at it's end
			return state == ArenaState.ProgressingWave || state == ArenaState.ClearingArena;
			// || state == ArenaState.Active;
		case Deactivated: // Inactive
			return state == ArenaState.Active;
		default:
			return false;
		}
	}

	public ArenaTimer getTimer() {
		return timer;
	}

	public void setTimer(ArenaTimer timer) {
		if (getTimer() != null) {
			getTimer().cancel();
		}
		this.timer = timer;
	}
}