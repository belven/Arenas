package belven.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class BossMob
{
    public EntityType BossType = EntityType.ZOMBIE;
    public LivingEntity le;

    public BossMob()
    {
        
    }

    public LivingEntity SpawnBoss(Location spawnLocation)
    {
        switch (BossType.name())
        {
        case "ZOMBIE":
            return SpawnZombieBoss(spawnLocation);
        case "SKELETON":
            return SpawnSkeletonBoss(spawnLocation);
        default:
            return SpawnZombieBoss(spawnLocation);
        }
    }

    public LivingEntity SpawnZombieBoss(Location spawnLocation)
    {
        le = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation,
                BossType);
        EntityEquipment ee = le.getEquipment();

        ItemStack meleeWeapon = new ItemStack(Material.IRON_SWORD);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack head = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack feet = new ItemStack(Material.DIAMOND_BOOTS);

        chest.addEnchantment(Enchantment.THORNS, 2);
        head.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
        legs.addEnchantment(Enchantment.PROTECTION_FIRE, 2);
        feet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        ee.setChestplate(chest);
        ee.setHelmet(head);
        ee.setLeggings(legs);
        ee.setBoots(feet);

        meleeWeapon.addEnchantment(Enchantment.DAMAGE_ALL, 2);

        le.getEquipment().setItemInHand(new ItemStack(meleeWeapon));
        return le;
    }

    public LivingEntity SpawnSkeletonBoss(Location spawnLocation)
    {
        ItemStack rangedWeapon = new ItemStack(Material.BOW);

        le = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation,
                BossType);

        EntityEquipment ee = le.getEquipment();

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack head = new ItemStack(Material.LEATHER_HELMET);
        ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack feet = new ItemStack(Material.LEATHER_BOOTS);

        rangedWeapon.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
        rangedWeapon.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        rangedWeapon.addEnchantment(Enchantment.ARROW_FIRE, 1);

        ee.setChestplate(chest);
        ee.setHelmet(head);
        ee.setLeggings(legs);
        ee.setBoots(feet);

        le.getEquipment().setItemInHand(new ItemStack(rangedWeapon));

        ee.setBootsDropChance(1);
        ee.setChestplateDropChance(1);
        ee.setLeggingsDropChance(1);
        ee.setHelmetDropChance(1);
        ee.setItemInHandDropChance(1);

        return le;

    }
}
