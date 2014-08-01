package belven.arena.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;
import belven.arena.resources.SavedBlock;

public abstract class ArenaBlock
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
        spawnAreaStartLocation = Functions.offsetLocation(startLocation, 0, -1,
                0);

        spawnAreaEndLocation = endLocation;

        blockToActivate = startLocation.getBlock();

        deactivateBlock = Functions.offsetLocation(startLocation, 0, 2, 0)
                .getBlock();

        LocationToCheckForPlayers = blockToActivate.getLocation();
        arenaWarp = startLocation.getBlock();
        radius = Radius;
        MobToMat = mobToMat;
        timerPeriod = TimerPeriod;
        arenaName = ArenaName;
        plugin = Plugin;
        maxRunTimes = 5;
    }

    public String ArenaName()
    {
        return ChatColor.RED + arenaName + ChatColor.WHITE;
    }

    public abstract void Activate();

    public abstract void Deactivate();

    public void RestoreArena()
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

    public abstract void GoToNextWave();

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

        arenaArea = Functions.getBlocksBetweenPoints(spawnAreaStartLocation,
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

}