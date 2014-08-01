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

import resources.Functions;
import resources.Gear;
import resources.MaterialFunctions;
import belven.arena.blocks.ArenaBlock;
import belven.arena.events.ArenaBlockNewWave;
import belven.arena.timedevents.MessageTimer;

public class Wave
{
    private ArenaBlock arenaBlock;
    Random randomGenerator = new Random();

    public Wave(ArenaBlock arenaBlock)
    {
        this.arenaBlock = arenaBlock;
        SpawnMobs();
        renewPlayerWeapons();

        Bukkit.getPluginManager().callEvent(new ArenaBlockNewWave(this));
    }

    public void SpawnMobs()
    {
        new MessageTimer(arenaBlock.arenaPlayers, ChatColor.RED
                + "Mobs Spawning: " + ChatColor.WHITE
                + String.valueOf(arenaBlock.maxMobCounter)).run();

        if (arenaBlock.spawnArea.size() > 0)
        {
            for (int mobCounter = 0; mobCounter < arenaBlock.maxMobCounter; mobCounter++)
            {
                int randomInt = randomGenerator.nextInt(arenaBlock.spawnArea
                        .size());
                Location spawnLocation = arenaBlock.spawnArea.get(randomInt)
                        .getLocation();
                MobToSpawn(spawnLocation);
            }

            if (arenaBlock.currentRunTimes == arenaBlock.maxRunTimes)
            {
                SpawnBoss();
            }
        }
    }

    public void renewPlayerWeapons()
    {
        for (Player p : arenaBlock.arenaPlayers)
        {
            boolean needsWeapon = true;

            for (ItemStack is : MaterialFunctions.getAllMeeleWeapons())
            {
                if (p.getInventory().contains(is.getType()))
                {
                    needsWeapon = false;
                    break;
                }
            }

            if (needsWeapon)
            {
                p.getInventory()
                        .addItem(new ItemStack(Material.STONE_SWORD, 1));
            }
        }
    }

    public void SpawnBoss()
    {
        int randomInt = randomGenerator.nextInt(arenaBlock.spawnArea.size());
        Location spawnLocation = arenaBlock.spawnArea.get(randomInt)
                .getLocation();

        LivingEntity currentEntity = arenaBlock.bm.SpawnBoss(spawnLocation);

        new MessageTimer(arenaBlock.arenaPlayers, "A "
                + arenaBlock.bm.BossType.name() + " boss has Spawned!!").run();

        // ScaleBossHealth(currentEntity);

        currentEntity.setMetadata("ArenaBoss", new FixedMetadataValue(
                arenaBlock.plugin, arenaBlock.arenaName));

        arenaBlock.ArenaEntities.add(currentEntity);
    }

    public void MobToSpawn(Location spawnLocation)
    {
        spawnLocation = Functions.offsetLocation(spawnLocation, 0.5, 0, 0.5);

        Block blockBelow = spawnLocation.getBlock().getRelative(BlockFace.DOWN);
        List<EntityType> et = new ArrayList<EntityType>();

        for (MobToMaterial mtm : arenaBlock.MobToMat.MobToMaterials)
        {
            if (blockBelow.getType() == mtm.m)
            {
                et.add(mtm.et);
            }
        }

        if (et.size() < 0)
        {
            return;
        }

        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(et.size());

        LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld()
                .spawnEntity(spawnLocation, et.get(randomInt));

        if (arenaBlock.currentRunTimes > 0 && arenaBlock.eliteWave > 0)
        {
            if (arenaBlock.currentRunTimes % arenaBlock.eliteWave == 0)
            {
                EliteMob(currentEntity);
            }
        }
        else if (currentEntity.getType() == EntityType.SKELETON)
        {
            currentEntity.getEquipment().setItemInHand(
                    new ItemStack(Material.BOW));
        }

        // ScaleMobHealth(currentEntity);

        currentEntity.setMetadata("ArenaMob", new FixedMetadataValue(
                arenaBlock.plugin, arenaBlock.arenaName));

        arenaBlock.ArenaEntities.add(currentEntity);
    }

    public void EliteMob(LivingEntity currentEntity)
    {
        if (arenaBlock.emc.Contains(currentEntity.getType()))
        {
            Gear gear = arenaBlock.emc.Get(currentEntity.getType()).armor;
            EntityEquipment ee = currentEntity.getEquipment();
            ee.setChestplate(gear.c);
            ee.setHelmet(gear.h);
            ee.setLeggings(gear.l);
            ee.setBoots(gear.b);
            ee.setItemInHand(gear.w);
        }
        else if (currentEntity.getType() == EntityType.SKELETON)
        {
            currentEntity.getEquipment().setItemInHand(
                    new ItemStack(Material.BOW));
        }
    }

    public void ScaleMobHealth(LivingEntity currentEntity)
    {
        double heathToscaleTo = Functions.MobMaxHealth(currentEntity)
                + (arenaBlock.averageLevel * 1.2);
        currentEntity.setMaxHealth(heathToscaleTo);
        currentEntity.setHealth(heathToscaleTo);
    }

    public void ScaleBossHealth(LivingEntity currentEntity)
    {
        double heathToscaleTo = Functions.MobMaxHealth(currentEntity)
                + (arenaBlock.averageLevel * 3);
        currentEntity.setMaxHealth(heathToscaleTo);
        currentEntity.setHealth(heathToscaleTo);
    }
}
