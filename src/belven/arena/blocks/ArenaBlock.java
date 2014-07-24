package belven.arena.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.resources.SavedBlock;
import belven.arena.resources.functions;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;

public class ArenaBlock
{
    public ArenaManager plugin;
    public boolean isActive = false;

    public String arenaName;
    public Block blockToActivate, deactivateBlock, arenaWarp;

    public List<Block> arenaArea = new ArrayList<Block>();
    public List<SavedBlock> originalBlocks = new ArrayList<SavedBlock>();
    // public List<Team> arenaTeams = new ArrayList<Team>();

    public List<Player> arenaPlayers = new ArrayList<Player>();
    public List<Block> spawnArea = new ArrayList<Block>();
    public List<ArenaBlock> linkedArenas = new ArrayList<ArenaBlock>();

    public Location LocationToCheckForPlayers, spawnAreaStartLocation,
            spawnAreaEndLocation;

    public int radius, maxRunTimes, timerPeriod, eliteWave, averageLevel,
            maxMobCounter, linkedArenaDelay, currentRunTimes = 0;

    public List<ItemStack> arenaRewards = new ArrayList<ItemStack>();
    public BossMob bm = new BossMob();
    public MobToMaterialCollecton MobToMat;
    public List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
    public EliteMobCollection emc = new EliteMobCollection(this);

    public UUID arenaRunID = null;

    public ArenaBlock(Location startLocation, Location endLocation,
            String ArenaName, int Radius, MobToMaterialCollecton mobToMat,
            ArenaManager Plugin, int TimerPeriod)
    {
        spawnAreaStartLocation = new Location(startLocation.getWorld(),
                startLocation.getX(), startLocation.getY() - 1,
                startLocation.getZ());

        spawnAreaEndLocation = endLocation;

        blockToActivate = startLocation.getBlock();

        deactivateBlock = startLocation.getWorld().getBlockAt(
                new Location(startLocation.getWorld(), startLocation.getX(),
                        startLocation.getY() + 2, startLocation.getZ()));

        LocationToCheckForPlayers = blockToActivate.getLocation();
        arenaWarp = startLocation.getBlock();
        radius = Radius;
        MobToMat = mobToMat;
        timerPeriod = TimerPeriod;
        arenaName = ArenaName;
        plugin = Plugin;
        maxRunTimes = 5;
    }

    public void Activate()
    {
        if (arenaPlayers.size() == 0)
        {
            Player[] tempPlayers = functions.getNearbyPlayersNew(
                    LocationToCheckForPlayers, (radius - 2) + (radius / 2));
            for (Player p : tempPlayers)
            {
                if (!plugin.IsPlayerInArena(p))
                {
                    plugin.WarpToArena(p, arenaName);
                }
            }
        }

        if (arenaPlayers.size() != 0)
        {
            arenaRunID = UUID.randomUUID();
            isActive = true;
            RemoveMobs();
            ArenaEntities.clear();
            GetArenaArea();
            GetSpawnArea();
            new ArenaTimer(this).runTaskLater(plugin, 10);
        }
    }

    public void Deactivate()
    {
        arenaRunID = null;
        RestoreArena();
        isActive = false;
        RemoveMobs();
        ArenaEntities.clear();
    }

    private void RestoreArena()
    {
        for (SavedBlock sb : originalBlocks)
        {
            sb.bs.update(true);
        }
    }

    public void GiveRewards()
    {
        Iterator<Player> ArenaPlayers = arenaPlayers.iterator();

        while (ArenaPlayers.hasNext())
        {
            Player p = ArenaPlayers.next();
            for (ItemStack is : arenaRewards)
            {
                if (is != null)
                {
                    p.getInventory().addItem(is);
                }
            }
        }
    }

    public void GoToNextWave()
    {
        if (arenaPlayers.size() > 0)
        {
            GetPlayersAverageLevel();
            currentRunTimes++;
            if (currentRunTimes == 1)
            {
                new MessageTimer(arenaPlayers, arenaName + " has Started!!")
                        .run();
            }
            new MessageTimer(arenaPlayers, arenaName + " Wave: "
                    + String.valueOf(currentRunTimes)).run();

            new Wave(this);

            new ArenaTimer(this).runTaskLater(plugin, timerPeriod);
        }
    }

    public void RemoveMobs()
    {
        currentRunTimes = 0;
        for (LivingEntity le : ArenaEntities)
        {
            if (!le.isDead())
            {
                le.removeMetadata("ArenaMob", plugin);
                le.setHealth(0.0);
            }
        }
    }

    public void GetArenaArea()
    {
        if (spawnAreaEndLocation == null)
        {
            plugin.getServer().getLogger().info("arenaBlockEndLocation NULL");
            return;
        }

        arenaArea = functions.getBlocksBetweenPoints(spawnAreaStartLocation,
                spawnAreaEndLocation);

        originalBlocks.clear();

        for (Block b : arenaArea)
        {
            originalBlocks.add(new SavedBlock(b));
        }
    }

    public void GetSpawnArea()
    {
        Location spawnLocation;
        spawnArea.clear();

        if (arenaArea != null && arenaArea.size() > 0)
        {
            for (Block b : arenaArea)
            {
                spawnLocation = b.getLocation();
                spawnLocation = CanSpawnAt(spawnLocation);

                if (spawnLocation != null && !b.equals(spawnAreaStartLocation))
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
                && MobToMat.Contains(blockBelow.getType()))
        {
            return currentLocation;
        }
        else
        {
            return null;
        }
    }

    public void GetPlayersAverageLevel()
    {
        if (arenaPlayers.size() == 0)
        {
            return;
        }

        int totalLevels = 0;
        averageLevel = 0;
        maxMobCounter = 0;

        for (Player p : arenaPlayers)
        {
            totalLevels += p.getLevel();
        }

        if (totalLevels == 0)
        {
            totalLevels = 1;
        }

        averageLevel = (int) (totalLevels / arenaPlayers.size());
        maxMobCounter = (int) (totalLevels / arenaPlayers.size())
                + (arenaPlayers.size() * 5);

        if (maxMobCounter > (arenaPlayers.size() * 15))
        {
            maxMobCounter = arenaPlayers.size() * 15;
        }
    }
}