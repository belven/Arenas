package belven.arena.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;
import belven.arena.resources.functions;
import belven.arena.timedevents.ArenaTimer;

public class ArenaBlock
{
    public ArenaManager plugin;
    public boolean isActive = false;

    public String arenaName, playersString;
    public Block blockToActivate, deactivateBlock, arenaWarp,
            arenaBlockStartLocation;

    public List<Block> arenaArea;
    public List<Player> arenaPlayers = new ArrayList<Player>();
    public List<Block> spawnArea = new ArrayList<Block>();

    public Location LocationToCheckForPlayers;

    public int radius, maxRunTimes, timerDelay, timerPeriod, eliteWave,
            averageLevel, maxMobCounter, currentRunTimes = 0;

    public BossMob bm = new BossMob();
    public MobToMaterialCollecton MobToMat;
    public List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
    public EliteMobCollection emc = new EliteMobCollection(this);

    public ArenaBlock(Block block, String ArenaName, Integer radius,
            MobToMaterialCollecton mobToMat, ArenaManager plugin,
            int timerDelay, int timerPeriod)
    {
        this.arenaBlockStartLocation = block.getWorld().getBlockAt(
                new Location(block.getWorld(), block.getX(), block.getY() - 1,
                        block.getZ()));

        this.blockToActivate = block;

        this.deactivateBlock = block.getWorld().getBlockAt(
                new Location(block.getWorld(), block.getX(), block.getY() + 2,
                        block.getZ()));

        this.LocationToCheckForPlayers = blockToActivate.getLocation();
        this.arenaWarp = block;
        this.radius = radius;
        this.MobToMat = mobToMat;
        this.timerDelay = timerDelay;
        this.timerPeriod = timerPeriod;
        this.arenaName = ArenaName;
        this.plugin = plugin;
        this.maxRunTimes = 5;
    }

    public void Activate()
    {
        isActive = true;
        RemoveMobs();
        ArenaEntities.clear();
        new ArenaTimer(this).runTaskLater(plugin, 10);
    }

    public void Deactivate()
    {
        isActive = false;
        RemoveMobs();
        ArenaEntities.clear();
    }

    public void RemoveMobs()
    {
        currentRunTimes = 0;
        for (LivingEntity le : ArenaEntities)
        {
            if (!le.isDead())
            {
                le.removeMetadata("ArenaMob", GetPlugin());
                le.setHealth(0);
            }
        }
    }

    public void GetArenaArea()
    {
        arenaArea = functions.getBlocksInRadius(
                this.arenaBlockStartLocation.getLocation(), this.radius);
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
                        && !b.equals(this.arenaBlockStartLocation))
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
                && this.MobToMat.Contains(blockBelow.getType()))
        {
            return currentLocation;
        }
        else
        {
            return null;
        }
    }

    public boolean GetPlayers()
    {
        Location areaToCheck = this.LocationToCheckForPlayers;
        Player[] currentPlayers = functions.getNearbyPlayers(areaToCheck,
                this.radius + (this.radius / 2));
        int totalLevels = 0;
        averageLevel = 0;
        maxMobCounter = 0;

        if (currentPlayers.length <= 0)
        {
            return false;
        }

        for (Player p : currentPlayers)
        {
            totalLevels += p.getLevel();

            if (!this.arenaPlayers.contains(p))
            {
                this.arenaPlayers.add(p);
                this.playersString = this.playersString + "," + p.getName();
            }
        }

        if (this.arenaPlayers.size() <= 0)
        {
            return false;
        }

        if (totalLevels == 0)
        {
            this.averageLevel = 1;
            this.maxMobCounter = 5;
        }
        else
        {
            this.averageLevel = (int) (totalLevels / currentPlayers.length);
            this.maxMobCounter = (int) (totalLevels / currentPlayers.length)
                    + (currentPlayers.length * 5);
        }

        return true;
    }

    public ArenaManager GetPlugin()
    {
        return plugin;
    }
}