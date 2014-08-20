package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import resources.Functions;
import belven.arena.blocks.StandardArenaBlock;
import belven.arena.events.ChallengeComplete;

public class Kills extends ChallengeType {
	public HashMap<EntityType, Integer> entitiesToKill = new HashMap<EntityType, Integer>();

	public Kills(HashMap<EntityType, Integer> entities) {
		type = ChallengeTypes.Kills;
		entitiesToKill = entities;
	}

	public void EntityKilled(EntityType et) {
		if (!ChallengeComplete()) {
			int amountLeft = entitiesToKill.get(et) != null ? entitiesToKill
					.get(et) : 0;

			if (amountLeft > 0) {
				amountLeft--;
				entitiesToKill.put(et, amountLeft);
			}

			if (ChallengeComplete()) {
				Bukkit.getPluginManager()
						.callEvent(new ChallengeComplete(this));
			}
		}
	}

	public List<String> ListRemainingEntities() {
		List<String> EntitiesLeft = new ArrayList<String>();
		for (EntityType et : entitiesToKill.keySet()) {
			EntitiesLeft.add(et.name() + ": "
					+ String.valueOf(entitiesToKill.get(et)));
		}
		return EntitiesLeft;
	}

	@Override
	public boolean ChallengeComplete() {
		int enitiesLeft = 0;

		for (EntityType et : entitiesToKill.keySet()) {
			enitiesLeft += entitiesToKill.get(et);
		}
		return enitiesLeft <= 0;
	}

	public static HashMap<EntityType, Integer> GetRandomEntities(
			StandardArenaBlock ab) {
		HashMap<EntityType, Integer> tempEntities = new HashMap<EntityType, Integer>();
		int amountOfEntities = new Random().nextInt(10) + 1;
		List<EntityType> entityTypes = ab.MobToMat.EntityTypes();

		for (int i = 0; i < amountOfEntities; i++) {
			EntityType et = entityTypes.get(new Random().nextInt(entityTypes
					.size()));
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

	public static void SpawnEntity(StandardArenaBlock ab, EntityType et) {
		int randomInt = new Random().nextInt(ab.spawnArea.size());
		Location spawnLocation = ab.spawnArea.get(randomInt).getLocation();
		spawnLocation = Functions.offsetLocation(spawnLocation, 0.5, 0, 0.5);

		LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld()
				.spawnEntity(spawnLocation, et);

		currentEntity.setMetadata("ArenaMob", new FixedMetadataValue(ab.plugin,
				ab.name));

		ab.ArenaEntities.add(currentEntity);
	}

}
