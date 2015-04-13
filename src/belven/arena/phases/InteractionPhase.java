package belven.arena.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import belven.arena.ArenaManager;
import belven.resources.Functions;

public class InteractionPhase extends Phase {
	private List<PhaseBlock> interactables = new ArrayList<>();

	public List<PhaseBlock> getInteractables() {
		return interactables;
	}

	public void setInteractables(List<PhaseBlock> interactables) {
		this.interactables = interactables;
	}

	public InteractionPhase(ArenaManager plugin, List<PhaseBlock> interactables) {
		this(plugin);
		setInteractables(interactables);
	}

	public InteractionPhase(ArenaManager plugin) {
		super(plugin);
	}

	@Override
	public Scoreboard GetPhaseScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sb = manager.getNewScoreboard();

		Objective objective = sb.registerNewObjective("test", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Interaction Phase");
		Score score = objective.getScore("Amount Left: ");
		score.setScore(getInteractables().size());
		return sb;
	}

	public void setRandomPhaseBlocks(List<Block> blocks) {
		List<Block> phasedBlocks = new ArrayList<Block>();
		for (int i = new Random().nextInt(3) + 1; i > 0; i--) {
			Block b = blocks.get(Functions.getRandomIndex(blocks));
			if (!phasedBlocks.contains(b)) {
				getInteractables().add(new PhaseBlock(this, b));
				phasedBlocks.add(b);
			}
		}
	}

	@Override
	public void activate() {
		setActive(true);
		for (PhaseBlock pb : getInteractables()) {
			pb.SetMetaData();
		}
	}

	@Override
	public void deactivate() {
		setActive(false);
	}

	public synchronized void interactionOccured(Interactable interactable) {
		if (getInteractables().contains(interactable)) {
			getInteractables().remove(interactable);
			if (getInteractables().size() <= 0) {
				deactivate();
			}
		}
	}
}
