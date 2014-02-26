package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.MobToMaterial;
import belven.arena.blocks.ArenaBlock;

public class ArenaTimer extends BukkitRunnable
{
    private List<Block> arenaArea;
    private List<Player> arenaPlayers = new ArrayList<Player>();
    private List<Block> spawnArea = new ArrayList<Block>();
    private int mobCounter = 0;
    // private int counter = 0;
    private int maxMobCounter = 0;
    private int currentRunTimes = 0;
    private int averageLevel = 0;
    private ArenaBlock arenaBlock;

    public ArenaTimer(ArenaBlock arenaBlock)
    {
        this.arenaBlock = arenaBlock;
        GetArenaArea();
        GetSpawnArea();
    }

    @Override
    public void run()
    {
        if (!GetPlayers())
        {
            new MessageTimer(arenaPlayers, "Arena " + arenaBlock.arenaName
                    + " has ended!!").run();
            arenaBlock.isActive = false;
            RemoveMobs();
            this.cancel();
        }
        else if (!arenaBlock.isActive)
        {
            new MessageTimer(arenaPlayers, "Arena " + arenaBlock.arenaName
                    + " has ended!!").run();
            RemoveMobs();
            this.cancel();
        }
        else if (currentRunTimes >= arenaBlock.maxRunTimes)
        {
            if (arenaBlock.ArenaEntities.size() > 0)
            {
                new MessageTimer(arenaPlayers, "Arena " + arenaBlock.arenaName
                        + " has "
                        + String.valueOf(arenaBlock.ArenaEntities.size())
                        + " mobs left").run();
                return;
            }

            new MessageTimer(arenaPlayers, "Arena " + arenaBlock.arenaName
                    + " has ended!!").run();
            RemoveMobs();
            arenaBlock.isActive = false;
            this.cancel();
        }
        else if (spawnArea.size() > 0
                && currentRunTimes < arenaBlock.maxRunTimes)
        {
            currentRunTimes++;
            if (currentRunTimes == 1)
            {
                new MessageTimer(arenaPlayers, arenaBlock.arenaName
                        + " has Started!!").run();
            }

            new MessageTimer(arenaPlayers, arenaBlock.arenaName + " Wave: "
                    + String.valueOf(currentRunTimes)).run();
            SpawnMobs();
        }
    }

    private void RemoveMobs()
    {
        for (LivingEntity le : arenaBlock.ArenaEntities)
        {
            if (!le.isDead())
            {
                le.removeMetadata("ArenaMob", arenaBlock.GetPlugin());
                le.setHealth(0);
            }
        }
    }

    public void GetArenaArea()
    {
        Location pointA = arenaBlock.arenaBlockStartLocation.getLocation();

        Location pointB = pointA;
        pointB.setY(pointA.getY() + arenaBlock.radius);
        pointB.setZ(pointA.getZ() + arenaBlock.radius);
        pointB.setX(pointA.getX() + arenaBlock.radius);

        arenaArea = getArenaBlocks(
                arenaBlock.arenaBlockStartLocation.getLocation(),
                arenaBlock.radius);

        // arenaArea = getArenaBlocks(
        // arenaBlock.arenaBlockStartLocation.getWorld(), pointA, pointB);
    }

    public void GetSpawnArea()
    {
        Location spawnLocation;
        spawnArea.clear();

        if (arenaArea.size() > 0)
        {
            for (Block b : arenaArea)
            {

                spawnLocation = b.getLocation();
                spawnLocation = CanSpawnAt(spawnLocation);

                if (spawnLocation != null
                        && !b.equals(arenaBlock.arenaBlockStartLocation))
                {
                    Block spawnBlock = spawnLocation.getBlock();
                    spawnArea.add(spawnBlock);
                }
            }
        }
    }

    private Location CanSpawnAt(Location currentLocation)
    {
        Block currentBlock = currentLocation.getBlock();
        Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
        Block blockAbove = currentBlock.getRelative(BlockFace.UP);

        if (currentBlock.getType() == Material.AIR
                && blockAbove.getType() == Material.AIR
                && arenaBlock.MobToMat.Contains(blockBelow.getType()))
        {
            return currentLocation;
        }
        else
        {
            return null;
        }
    }

    private void SpawnMobs()
    {
        Random randomGenerator = new Random();
        new MessageTimer(arenaPlayers, "Mobs Spawning: "
                + String.valueOf(maxMobCounter)).run();

        for (mobCounter = 0; mobCounter < maxMobCounter; mobCounter++)
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
        int heathToscaleTo = (int) (MobMaxHealth(currentEntity) + (averageLevel * 1.2));
        currentEntity.setMaxHealth(heathToscaleTo * 3);
        currentEntity.setHealth(heathToscaleTo * 3);
    }

    public boolean GetPlayers()
    {
        Location areaToCheck = arenaBlock.LocationToCheckForPlayers;
        Player[] currentPlayers = getNearbyPlayers(areaToCheck,
                arenaBlock.radius + (arenaBlock.radius / 2));
        int totalLevels = 0;
        averageLevel = 0;
        maxMobCounter = 0;

        if (currentPlayers.length > 0)
        {
            for (Player p : currentPlayers)
            {
                totalLevels += p.getLevel();

                if (!arenaPlayers.contains(p))
                {
                    arenaPlayers.add(p);
                    arenaBlock.playersString = arenaBlock.playersString
                            + p.getName() + " ";
                }
            }

            if (arenaPlayers.size() > 0)
            {
                if (totalLevels == 0)
                {
                    averageLevel = 1;
                    maxMobCounter = 5;
                }
                else
                {
                    averageLevel = (int) (totalLevels / currentPlayers.length);
                    maxMobCounter = (int) (totalLevels / currentPlayers.length)
                            + (currentPlayers.length * 5);
                }

                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private static Player[] getNearbyPlayers(Location l, int radius)
    {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Entity> radiusEntities = new HashSet<Entity>();

        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
        {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++)
            {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();

                for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z
                        + (chZ * 16)).getChunk().getEntities())
                {
                    if (e.getLocation().distance(l) <= radius
                            && e instanceof Player
                            && e.getLocation().getBlock() != l.getBlock())
                    {
                        radiusEntities.add((Player) e);
                    }
                }
            }
        }

        return radiusEntities.toArray(new Player[radiusEntities.size()]);
    }

    private static List<Block> getArenaBlocks(Location l, int radius)
    {
        World w = l.getWorld();
        int xCoord = (int) l.getX();
        int zCoord = (int) l.getZ();
        int YCoord = (int) l.getY();

        List<Block> tempList = new ArrayList<Block>();
        for (int x = 0; x <= 2 * radius; x++)
        {
            for (int z = 0; z <= 2 * radius; z++)
            {
                for (int y = 0; y <= 2 * radius; y++)
                {
                    tempList.add(w.getBlockAt(xCoord + x, YCoord + y, zCoord
                            + z));
                }
            }
        }
        return tempList;
    }

    public int MobMaxHealth(LivingEntity entity)
    {
        if (entity.getType() == EntityType.ZOMBIE)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.SKELETON)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.SPIDER)
        {
            return 16;
        }
        else if (entity.getType() == EntityType.CREEPER)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.WITHER)
        {
            return 300;
        }
        else if (entity.getType() == EntityType.BLAZE)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.ENDERMAN)
        {
            return 40;
        }
        else if (entity.getType() == EntityType.CAVE_SPIDER)
        {
            return 12;
        }
        else if (entity.getType() == EntityType.GHAST)
        {
            return 10;
        }
        else if (entity.getType() == EntityType.MAGMA_CUBE)
        {
            MagmaCube MagmaCube = (MagmaCube) entity;

            if (MagmaCube.getSize() == 4)

            {
                return 16;
            }
            else if (MagmaCube.getSize() == 2)
            {
                return 4;
            }
            else
            {
                return 1;
            }
        }
        else if (entity.getType() == EntityType.PIG_ZOMBIE)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.SLIME)
        {
            Slime slime = (Slime) entity;

            if (slime.getSize() == 4)

            {
                return 16;
            }
            else if (slime.getSize() == 2)
            {
                return 4;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return 20;
        }
    }
}
