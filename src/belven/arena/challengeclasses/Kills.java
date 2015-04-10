package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import belven.arena.MDM;
import belven.arena.arenas.StandardArena;
import belven.arena.events.ChallengeComplete;
import belven.resources.Functions;

public class Kills extends ChallengeType {
	public static HashMap<EntityType, Integer> GetRandomEntities(StandardArena ab) {
		HashMap<EntityType, Integer> tempEntities = new HashMap<EntityType, Integer>();
		int amountOfEntities = new Random().nextInt(10) + 1;
		List<EntityType> entityTypes = ab.getMobToMat().EntityTypes();

		for (int i = 0; i < amountOfEntities; i++) {
			EntityType et = entityTypes.get(new Random().nextInt(entityTypes.size()));
			SpawnEntity(ab, et);

			if (tempEntities.containsKey(et)) {
				int amount = tempEntities.get(et) + 1;
				tempEntities.put(et, amount);
			} else {
				tempEntities.put(et, 1);
			}
		}

		return tempEntities;
	}

	public static void SpawnEntity(StandardArena ab, EntityType et) {
		int randomInt = new Random().nextInt(ab.getSpawnArea().size());
		Location spawnLocation = ab.getSpawnArea().get(randomInt).getLocation();
		spawnLocation = Functions.offsetLocation(spawnLocation, 0.5, 0, 0.5);

		LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, et);

		currentEntity.setMetadata(MDM.ArenaMob, new FixedMetadataValue(ab.getPlugin(), ab));

		ab.getArenaEntities().add(currentEntity);
	}

	public HashMap<EntityType, Integer> entitiesToKill = new HashMap<EntityType, Integer>();

	public Kills(ChallengeBlock cb, HashMap<EntityType, Integer> entities) {
		super(cb);
		type = ChallengeTypes.Kills;
		entitiesToKill = entities;
	}

	@Override
	public boolean ChallengeComplete() {
		int enitiesLeft = 0;

		for (EntityType et : entitiesToKill.keySet()) {
			enitiesLeft += entitiesToKill.get(et);
		}
		return enitiesLeft <= 0;
	}

	@Override
	public void EntityKilled(EntityType et) {
		if (!ChallengeComplete()) {
			int amountLeft = entitiesToKill.get(et) != null ? entitiesToKill.get(et) : 0;

			if (amountLeft > 0) {
				amountLeft--;
				entitiesToKill.put(et, amountLeft);
			}

			getChallengeBlock().SetPlayersScoreboard();

			if (ChallengeComplete()) {
				Bukkit.getPluginManager().callEvent(new ChallengeComplete(this));
			}
		}
	}

	public List<String> ListRemainingEntities() {
		List<String> EntitiesLeft = new ArrayList<String>();
		for (EntityType et : entitiesToKill.keySet()) {
			EntitiesLeft.add(et.name() + ": " + String.valueOf(entitiesToKill.get(et)));
		}
		return EntitiesLeft;
	}

	@Override
	public Scoreboard SetChallengeScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sb = manager.getNewScoreboard();

		if (!ChallengeComplete()) {
			Objective objective = sb.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			objective.setDisplayName("Kill Challenge");

			for (EntityType et : entitiesToKill.keySet()) {
				Score score = objective.getScore(et.name());
				score.setScore(entitiesToKill.get(et));
			}
		}

		return sb;
	}

	@Override
	public boolean ChallengeBlockInteracted(Player p) {
		// TODO Auto-generated method stub
		return false;
	}

}
