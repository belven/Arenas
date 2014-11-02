package belven.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import resources.EntityFunctions;
import resources.Gear;
import resources.MaterialFunctions;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.StandardArena;
import belven.arena.events.ArenaBlockNewWave;
import belven.arena.timedevents.MessageTimer;

public class Wave {
	private StandardArena ab;
	Random randomGenerator = new Random();

	public Wave(StandardArena arenaBlock) {
		this.ab = arenaBlock;
		SpawnMobs();
		renewPlayerWeapons();

		Bukkit.getPluginManager().callEvent(new ArenaBlockNewWave(this));
	}

	public void EliteMob(LivingEntity currentEntity) {
		if (ab.getEliteMobCollection().Contains(currentEntity.getType())) {
			Gear gear = ab.getEliteMobCollection().Get(currentEntity.getType()).armor;
			currentEntity.getEquipment();
			gear.SetGear(currentEntity);
		} else if (currentEntity.getType() == EntityType.SKELETON) {
			currentEntity.getEquipment().setItemInHand(new ItemStack(Material.BOW));
		}
	}

	public void MobToSpawn(Location spawnLocation) {
		Block blockBelow = spawnLocation.getBlock().getRelative(BlockFace.DOWN);
		List<EntityType> et = new ArrayList<EntityType>();

		for (MobToMaterial mtm : ab.getMobToMat().MobToMaterials) {
			if (blockBelow.getType() == mtm.m) {
				et.add(mtm.et);
			}
		}

		if (et.size() == 0) {
			return;
		}

		int rand = new Random().nextInt(et.size());

		if (rand > 0) {
			rand--;
		}

		LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, et.get(rand));

		if (ab.getCurrentRunTimes() > 0 && ab.getEliteWave() > 0) {
			if (ab.getCurrentRunTimes() % ab.getEliteWave() == 0) {
				EliteMob(currentEntity);
			}
		} else if (currentEntity.getType() == EntityType.SKELETON) {
			currentEntity.getEquipment().setItemInHand(new ItemStack(Material.BOW));
		}

		currentEntity.setMetadata(MDM.ArenaMob, new FixedMetadataValue(ab.getPlugin(), ab));
		ab.getArenaEntities().add(currentEntity);
	}

	public void renewPlayerWeapons() {
		for (Player p : ab.getArenaPlayers()) {
			boolean needsWeapon = true;

			for (ItemStack is : MaterialFunctions.getAllMeeleWeapons()) {
				if (p.getInventory().contains(is.getType())) {
					needsWeapon = false;
					break;
				}
			}

			if (needsWeapon) {
				p.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
			}
		}
	}

	public void ScaleBossHealth(LivingEntity currentEntity) {
		double heathToscaleTo = EntityFunctions.MobMaxHealth(currentEntity) + ab.getAverageLevel() * 3;
		currentEntity.setMaxHealth(heathToscaleTo);
		currentEntity.setHealth(heathToscaleTo);
	}

	public void ScaleMobHealth(LivingEntity currentEntity) {
		double heathToscaleTo = EntityFunctions.MobMaxHealth(currentEntity) + ab.getAverageLevel() * 1.2;
		currentEntity.setMaxHealth(heathToscaleTo);
		currentEntity.setHealth(heathToscaleTo);
	}

	public void SpawnBoss() {

		LivingEntity le = ab.getBossMob().SpawnBoss(BaseArena.GetRandomArenaSpawnLocation(ab));

		Gear gear = ArenaManager.scalingGear.get(ab.getArenaPlayers().size());

		if (gear != null && le != null) {
			gear.SetGear(le);
		}

		new MessageTimer(ab.getArenaPlayers(), "A " + ab.getBossMob().BossType.name() + " boss has Spawned!!").run();

		le.setMetadata(MDM.ArenaBoss, new FixedMetadataValue(ab.getPlugin(), ab));
		ab.getArenaEntities().add(le);
	}

	public void SpawnMobs() {
		new MessageTimer(ab.getArenaPlayers(), ChatColor.RED + "Mobs Spawning: " + ChatColor.WHITE
				+ String.valueOf(ab.getMaxMobCounter())).run();

		if (ab.getSpawnArea().size() > 0) {
			for (int mobCounter = 0; mobCounter < ab.getMaxMobCounter(); mobCounter++) {
				MobToSpawn(BaseArena.GetRandomArenaSpawnLocation(ab));
			}

			if (ab.getCurrentRunTimes() == ab.getMaxRunTimes()) {
				SpawnBoss();
			}
		}
	}
}
