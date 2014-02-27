package belven.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import belven.arena.blocks.ArenaBlock;
import belven.arena.resources.functions;
import belven.arena.timedevents.MessageTimer;

public class Wave
{
    private List<Block> spawnArea;
    private List<Player> arenaPlayers;
    private ArenaBlock arenaBlock;
    private int maxMobCounter;
    private int currentRunTimes;
    private double averageLevel;

    public Wave(List<Block> spawnArea, List<Player> arenaPlayers,
            ArenaBlock arenaBlock, int maxMobCounter, int currentRunTimes,
            double averageLevel)
    {
        this.spawnArea = spawnArea;
        this.arenaPlayers = arenaPlayers;
        this.arenaBlock = arenaBlock;
        this.maxMobCounter = maxMobCounter;
        this.currentRunTimes = currentRunTimes;
        this.averageLevel = averageLevel;
        SpawnMobs();
    }

    public void SpawnMobs()
    {
        Random randomGenerator = new Random();
        new MessageTimer(arenaPlayers, "Mobs Spawning: "
                + String.valueOf(maxMobCounter)).run();

        for (int mobCounter = 0; mobCounter < maxMobCounter; mobCounter++)
        {
            int randomInt = randomGenerator.nextInt(spawnArea.size());
            Location spawnLocation = spawnArea.get(randomInt).getLocation();
            MobToSpawn(spawnLocation);
        }

        if (currentRunTimes >= arenaBlock.maxRunTimes)
        {
            int randomInt = randomGenerator.nextInt(spawnArea.size());
            Location spawnLocation = spawnArea.get(randomInt).getLocation();

            LivingEntity currentEntity = arenaBlock.bm.SpawnBoss(spawnLocation);

            new MessageTimer(arenaPlayers, "A " + arenaBlock.bm.BossType.name()
                    + " boss has Spawned!!").run();

            ScaleMobHealth(currentEntity);

            currentEntity.setMetadata("ArenaMob", new FixedMetadataValue(
                    arenaBlock.GetPlugin(), arenaBlock.arenaName + " "
                            + arenaBlock.playersString));

            arenaBlock.ArenaEntities.add(currentEntity);
        }
    }

    public void MobToSpawn(Location spawnLocation)
    {
        Block blockBelow = spawnLocation.getBlock().getRelative(BlockFace.DOWN);
        List<EntityType> et = new ArrayList<EntityType>();

        for (MobToMaterial mtm : arenaBlock.MobToMat.MobToMaterials)
        {
            if (blockBelow.getType() == mtm.m)
            {
                et.add(mtm.et);
            }
        }

        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(et.size());

        LivingEntity currentEntity = (LivingEntity) spawnLocation.getWorld()
                .spawnEntity(spawnLocation, et.get(randomInt));

        currentEntity.setCanPickupItems(true);

        if (currentRunTimes == arenaBlock.eliteWave)
        {
            EliteMob(currentEntity);
        }
        else if (currentEntity.getType() == EntityType.SKELETON)
        {
            currentEntity.getEquipment().setItemInHand(
                    new ItemStack(Material.BOW));
        }

        ScaleMobHealth(currentEntity);

        currentEntity.setMetadata("ArenaMob",
                new FixedMetadataValue(arenaBlock.GetPlugin(),
                        arenaBlock.arenaName + " " + arenaBlock.playersString));

        arenaBlock.ArenaEntities.add(currentEntity);
    }

    public void EliteMob(LivingEntity currentEntity)
    {
        if (arenaBlock.emc.Contains(currentEntity.getType()))
        {
            List<ItemStack> gear = arenaBlock.emc.Get(currentEntity.getType()).gear;
            EntityEquipment ee = currentEntity.getEquipment();
            ee.setChestplate(gear.get(0));
            ee.setHelmet(gear.get(1));
            ee.setLeggings(gear.get(2));
            ee.setBoots(gear.get(3));
            ee.setItemInHand(gear.get(4));
        }
    }

    public void ScaleMobHealth(LivingEntity currentEntity)
    {
        int heathToscaleTo = (int) (functions.MobMaxHealth(currentEntity) + (averageLevel * 1.2));
        currentEntity.setMaxHealth(heathToscaleTo * 3);
        currentEntity.setHealth(heathToscaleTo * 3);
    }
}
