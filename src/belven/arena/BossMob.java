package belven.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class BossMob
{
    public EntityType BossType = EntityType.ZOMBIE;
    public LivingEntity le;
    public List<ItemStack> gear = new ArrayList<ItemStack>();

    public LivingEntity SpawnBoss(Location spawnLocation)
    {
        le = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation,
                BossType);
        EntityEquipment ee = le.getEquipment();
        
        if (gear.size() > 0)
        {
            ee.setChestplate(gear.get(0));
            ee.setHelmet(gear.get(1));
            ee.setLeggings(gear.get(2));
            ee.setBoots(gear.get(3));
            ee.setItemInHand(gear.get(4));

            ee.setBootsDropChance(1F);
            ee.setChestplateDropChance(1F);
            ee.setLeggingsDropChance(1F);
            ee.setHelmetDropChance(1F);
            ee.setItemInHandDropChance(1F);
        }

        return le;
    }
}
