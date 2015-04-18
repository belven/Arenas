package belven.arena.phases;

import java.util.List;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.scoreboard.Scoreboard;

import belven.arena.ArenaManager;
import belven.arena.arenas.Phaseable;

public abstract class Phase {
	private boolean active = false;
	private ArenaManager plugin;
	private int phaseDuration = 30;
	private Phaseable owner;

	public Phase(ArenaManager plugin, Phaseable owner) {
		setPlugin(plugin);
		setOwner(owner);
	}

	public abstract Scoreboard GetPhaseScoreboard();

	public abstract void activate();

	public abstract void deactivate();

	public abstract boolean isCompleted();

	public static Phase getRandomPhase(ArenaManager plugin, Phaseable owner, List<Block> blocks) {
		InteractionPhase ip;
		int rand = new Random().nextInt(3);

		switch (rand) {
		case 0:
			ip = new InteractionPhase(plugin, owner);
			ip.setRandomPhaseBlocks(blocks);
			return ip;
		case 1:
			KillsPhase kp = new KillsPhase(plugin, owner);
			kp.setBlocks(blocks);
			return kp;
		default:
			KillsPhase kp1 = new KillsPhase(plugin, owner);
			kp1.setBlocks(blocks);
			return kp1;
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

	public int getPhaseDuration() {
		return phaseDuration;
	}

	public void setPhaseDuration(int phaseDuration) {
		this.phaseDuration = phaseDuration;
	}

	public Phaseable getOwner() {
		return owner;
	}

	public void setOwner(Phaseable owner) {
		this.owner = owner;
	}

}
