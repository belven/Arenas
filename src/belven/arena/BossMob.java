package belven.arena;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import resources.Gear;

public class BossMob {
	public EntityType BossType = EntityType.ZOMBIE;
	public LivingEntity le;
	public Gear gear;

	public LivingEntity SpawnBoss(Location spawnLocation) {
		le = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation,
				BossType);
		EntityEquipment ee = le.getEquipment();
		gear.SetGear(le);

		ee.setBootsDropChance(1F);
		ee.setChestplateDropChance(1F);
		ee.setLeggingsDropChance(1F);
		ee.setHelmetDropChance(1F);
		ee.setItemInHandDropChance(1F);
		return le;
	}
}
