package belven.arena.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.scoreboard.Scoreboard;

import belven.arena.ArenaManager;

public abstract class Phase {
	private boolean active = false;
	private ArenaManager plugin;
	private List<Block> phaseBlocks = new ArrayList<Block>();
	private int phaseDuration;

	public Phase(ArenaManager plugin, List<Block> phaseBlocks) {
		this(plugin);
		setPhaseBlocks(phaseBlocks);
	}

	public Phase(ArenaManager plugin) {
		setPlugin(plugin);
	}

	public abstract Scoreboard GetPhaseScoreboard();

	public abstract void activate();

	public abstract void deactivate();

	public static Phase getRandomPhase(ArenaManager plugin, List<Block> blocks) {
		InteractionPhase ip;
		int rand = new Random().nextInt(1);

		switch (rand) {
		case 0:
			ip = new InteractionPhase(plugin);
			ip.setRandomPhaseBlocks(blocks);
			return ip;
		default:
			ip = new InteractionPhase(plugin);
			ip.setRandomPhaseBlocks(blocks);
			return ip;
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ArenaManager getPlugin() {
		return plugin;
	}

	public void setPlugin(ArenaManager plugin) {
		this.plugin = plugin;
	}

	public List<Block> getPhaseBlocks() {
		return phaseBlocks;
	}

	public void setPhaseBlocks(List<Block> phaseBlocks) {
		this.phaseBlocks = phaseBlocks;
	}

	public int getPhaseDuration() {
		return phaseDuration;
	}

	public void setPhaseDuration(int phaseDuration) {
		this.phaseDuration = phaseDuration;
	}

}
