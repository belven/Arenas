package belven.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
        ItemStack rangedWeapon = new ItemStack(Material.BOW);
        ItemStack meleeWeapon = new ItemStack(Material.IRON_SWORD);

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

        if (currentEntity.getType() == EntityType.SKELETON)
        {
            if (currentRunTimes == arenaBlock.eliteWave)
            {
                rangedWeapon.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
            }
            currentEntity.getEquipment().setItemInHand(
                    new ItemStack(rangedWeapon));
        }
        else if (currentEntity.getType() == EntityType.ZOMBIE
                && currentRunTimes == arenaBlock.eliteWave)
        {
            meleeWeapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            currentEntity.getEquipment().setItemInHand(
                    new ItemStack(meleeWeapon));
        }

        ScaleMobHealth(currentEntity);

        currentEntity.setMetadata("ArenaMob",
                new FixedMetadataValue(arenaBlock.GetPlugin(),
                        arenaBlock.arenaName + " " + arenaBlock.playersString));

        arenaBlock.ArenaEntities.add(currentEntity);
    }

    public void ScaleMobHealth(LivingEntity currentEntity)
    {
        int heathToscaleTo = (int) (functions.MobMaxHealth(currentEntity) + (averageLevel * 1.2));
        currentEntity.setMaxHealth(heathToscaleTo * 3);
        currentEntity.setHealth(heathToscaleTo * 3);
    }
}
