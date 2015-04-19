package belven.arena.phases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.arenas.Phaseable;
import belven.resources.Functions;

public class KillsPhase extends Phase {
	public static final String KILLS_PHASE = "KillsPhase";
	private HashMap<EntityType, Integer> entitiesToKill = new HashMap<>();
	private List<LivingEntity> entities = new ArrayList<>();

	private List<Block> blocks = new ArrayList<>();
	private MobToMaterialCollecton mtm = new MobToMaterialCollecton();

	public KillsPhase(ArenaManager plugin, Phaseable owner, List<Block> blocks, MobToMaterialCollecton mtm) {
		this(plugin, owner);
		setBlocks(blocks);
	}

	public KillsPhase(ArenaManager plugin, Phaseable owner) {
		super(plugin, owner);
	}

	@Override
	public Scoreboard GetPhaseScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sb = manager.getNewScoreboard();

		Objective objective = sb.registerNewObjective("test", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		objective.setDisplayName("Kill Phase");

		for (EntityType et : entitiesToKill.keySet()) {
			Score score = objective.getScore(et.name());
			score.setScore(entitiesToKill.get(et));
		}

		return sb;
	}

	public void spawnEntities() {
		int amountOfEntities = new Random().nextInt(10) + 1;
		List<EntityType> entityTypes = mtm.EntityTypes();

		for (int i = 0; i < amountOfEntities; i++) {
			EntityType et = entityTypes.get(Functions.getRandomIndex(entityTypes));
			SpawnEntity(et);

			if (entitiesToKill.containsKey(et)) {
				int amount = entitiesToKill.get(et) + 1;
				entitiesToKill.put(et, amount);
			} else {
				entitiesToKill.put(et, 1);
			}
		}
	}

	public void SpawnEntity(EntityType et) {
		Location spawnLocation = getBlocks().get(Functions.getRandomIndex(getBlocks())).getLocation();
		spawnLocation = Functions.offsetLocation(spawnLocation, 0.5, 0, 0.5);
		LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, et);
		currentEntity.setMetadata(KILLS_PHASE, new FixedMetadataValue(getPlugin(), this));
		entities.add(currentEntity);
	}

	@Override
	public void activate() {
		setActive(true);

		if (mtm.MobToMaterials.size() <= 0) {
			setMtm(ArenaManager.MatToMob(getBlocks().get(Functions.getRandomIndex(getBlocks())).getType()));
		}

		spawnEntities();
		getOwner().PhaseChanged(this);
	}

	@Override
	public void deactivate() {
		setActive(false);
		if (!isCompleted()) {
			for (LivingEntity le : getEntities()) {
				le.remove();
			}
		}
		entities.clear();
		entitiesToKill.clear();
		getOwner().PhaseChanged(this);
	}

	private List<LivingEntity> getEntities() {
		return entities;
	}

	@Override
	public boolean isCompleted() {
		return getEntities().size() <= 0;
	}

	public synchronized void EntityKilled(Entity e) {
		int amountLeft = entitiesToKill.get(e.getType()) != null ? entitiesToKill.get(e.getType()) : 0;

		if (amountLeft > 0) {
			amountLeft--;
			entitiesToKill.put(e.getType(), amountLeft);
		}

		if (entities.contains(e)) {
			entities.remove(e);
		}

		if (isCompleted()) {
			deactivate();
		} else {
			getOwner().PhaseChanged(this);
		}
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<Block> blocks) {
		this.blocks = blocks;
	}

	public MobToMaterialCollecton getMtm() {
		return mtm;
	}

	public void setMtm(MobToMaterialCollecton mtm) {
		this.mtm = mtm;
	}

	public void setEntities(List<LivingEntity> entities) {
		this.entities = entities;
	}

	@Override
	public boolean phaseRanDuration() {
		for (LivingEntity le : entities) {
			Location spawnLocation = getBlocks().get(Functions.getRandomIndex(getBlocks())).getLocation();
			le.teleport(spawnLocation);
		}
		return true;
	}
}
