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
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import resources.EntityFunctions;
import resources.Gear;
import resources.MaterialFunctions;
import belven.arena.blocks.ArenaBlock;
import belven.arena.blocks.StandardArenaBlock;
import belven.arena.events.ArenaBlockNewWave;
import belven.arena.timedevents.MessageTimer;

public class Wave {
	private StandardArenaBlock ab;
	Random randomGenerator = new Random();

	public Wave(StandardArenaBlock arenaBlock) {
		this.ab = arenaBlock;
		SpawnMobs();
		renewPlayerWeapons();

		Bukkit.getPluginManager().callEvent(new ArenaBlockNewWave(this));
	}

	public void SpawnMobs() {
		new MessageTimer(ab.arenaPlayers, ChatColor.RED + "Mobs Spawning: "
				+ ChatColor.WHITE + String.valueOf(ab.maxMobCounter)).run();

		if (ab.spawnArea.size() > 0) {
			for (int mobCounter = 0; mobCounter < ab.maxMobCounter; mobCounter++) {
				MobToSpawn(ArenaBlock.GetRandomArenaSpawnLocation(ab));
			}

			if (ab.currentRunTimes == ab.maxRunTimes) {
				SpawnBoss();
			}
		}
	}

	public void renewPlayerWeapons() {
		for (Player p : ab.arenaPlayers) {
			boolean needsWeapon = true;

			for (ItemStack is : MaterialFunctions.getAllMeeleWeapons()) {
				if (p.getInventory().contains(is.getType())) {
					needsWeapon = false;
					break;
				}
			}

			if (needsWeapon) {
				p.getInventory()
						.addItem(new ItemStack(Material.STONE_SWORD, 1));
			}
		}
	}

	public void SpawnBoss() {
		int randomInt = randomGenerator.nextInt(ab.spawnArea.size());
		Location spawnLocation = ab.spawnArea.get(randomInt).getLocation();

		LivingEntity le = ab.bm.SpawnBoss(spawnLocation);

		Gear bossGear = ArenaManager.scalingGear.get(ab.arenaPlayers.size());
		le.getEquipment().setHelmet(bossGear.h);
		le.getEquipment().setChestplate(bossGear.c);
		le.getEquipment().setLeggings(bossGear.l);
		le.getEquipment().setBoots(bossGear.b);
		le.getEquipment().setItemInHand(bossGear.w);

		new MessageTimer(ab.arenaPlayers, "A " + ab.bm.BossType.name()
				+ " boss has Spawned!!").run();

		le.setMetadata("ArenaBoss", new FixedMetadataValue(ab.plugin, ab.name));
		ab.ArenaEntities.add(le);
	}

	public void MobToSpawn(Location spawnLocation) {
		Block blockBelow = spawnLocation.getBlock().getRelative(BlockFace.DOWN);
		List<EntityType> et = new ArrayList<EntityType>();

		for (MobToMaterial mtm : ab.MobToMat.MobToMaterials) {
			if (blockBelow.getType() == mtm.m) {
				et.add(mtm.et);
			}
		}

		if (et.size() == 0)
			return;

		int rand = new Random().nextInt(et.size());

		if (rand > 0)
			rand--;

		LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld()
				.spawnEntity(spawnLocation, et.get(rand));

		if (ab.currentRunTimes > 0 && ab.eliteWave > 0) {
			if (ab.currentRunTimes % ab.eliteWave == 0) {
				EliteMob(currentEntity);
			}
		} else if (currentEntity.getType() == EntityType.SKELETON) {
			currentEntity.getEquipment().setItemInHand(
					new ItemStack(Material.BOW));
		}

		currentEntity.setMetadata("ArenaMob", new FixedMetadataValue(ab.plugin,
				ab.name));
		ab.ArenaEntities.add(currentEntity);
	}

	public void EliteMob(LivingEntity currentEntity) {
		if (ab.emc.Contains(currentEntity.getType())) {
			Gear gear = ab.emc.Get(currentEntity.getType()).armor;
			EntityEquipment ee = currentEntity.getEquipment();
			ee.setChestplate(gear.c);
			ee.setHelmet(gear.h);
			ee.setLeggings(gear.l);
			ee.setBoots(gear.b);
			ee.setItemInHand(gear.w);
		} else if (currentEntity.getType() == EntityType.SKELETON) {
			currentEntity.getEquipment().setItemInHand(
					new ItemStack(Material.BOW));
		}
	}

	public void ScaleMobHealth(LivingEntity currentEntity) {
		double heathToscaleTo = EntityFunctions.MobMaxHealth(currentEntity)
				+ (ab.averageLevel * 1.2);
		currentEntity.setMaxHealth(heathToscaleTo);
		currentEntity.setHealth(heathToscaleTo);
	}

	public void ScaleBossHealth(LivingEntity currentEntity) {
		double heathToscaleTo = EntityFunctions.MobMaxHealth(currentEntity)
				+ (ab.averageLevel * 3);
		currentEntity.setMaxHealth(heathToscaleTo);
		currentEntity.setHealth(heathToscaleTo);
	}
}
