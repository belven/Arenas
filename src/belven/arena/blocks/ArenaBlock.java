package belven.arena.blocks;

import java.util.ArrayList;
import java.util.List;

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
import belven.arena.resources.functions;
import belven.arena.timedevents.ArenaTimer;

public class ArenaBlock
{
    public ArenaManager plugin;
    public boolean isActive = false;

    public String arenaName, playersString;
    public Block blockToActivate, deactivateBlock, arenaWarp;

    public List<Block> arenaArea;
    public List<Player> arenaPlayers = new ArrayList<Player>();
    public List<Block> spawnArea = new ArrayList<Block>();

    public Location LocationToCheckForPlayers, arenaBlockStartLocation,
            arenaBlockEndLocation;

    public int radius, maxRunTimes, timerDelay, timerPeriod, eliteWave,
            averageLevel, maxMobCounter, currentRunTimes = 0;

    public List<ItemStack> arenaRewards = new ArrayList<ItemStack>();
    public BossMob bm = new BossMob();
    public MobToMaterialCollecton MobToMat;
    public List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
    public EliteMobCollection emc = new EliteMobCollection(this);

    public ArenaBlock(Location startLocation, Location endLocation,
            String ArenaName, Integer radius, MobToMaterialCollecton mobToMat,
            ArenaManager plugin, int timerDelay, int timerPeriod)
    {
        this.arenaBlockStartLocation = new Location(startLocation.getWorld(),
                startLocation.getX(), startLocation.getY() - 1,
                startLocation.getZ());

        this.arenaBlockEndLocation = endLocation;

        this.blockToActivate = startLocation.getBlock();

        this.deactivateBlock = startLocation.getWorld().getBlockAt(
                new Location(startLocation.getWorld(), startLocation.getX(),
                        startLocation.getY() + 2, startLocation.getZ()));

        this.LocationToCheckForPlayers = blockToActivate.getLocation();
        this.arenaWarp = startLocation.getBlock();
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
        arenaPlayers.clear();
        ArenaEntities.clear();
        new ArenaTimer(this).runTaskLater(plugin, 10);
    }

    public void Deactivate()
    {
        isActive = false;
        RemoveMobs();
        ArenaEntities.clear();
    }

    public void GiveRewards()
    {
        for (Player p : arenaPlayers)
        {
            for (ItemStack is : arenaRewards)
            {
                if (is != null)
                {
                    p.getInventory().addItem(is);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
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
        if (this.arenaBlockEndLocation == null)
        {
            plugin.getServer().getLogger().info("arenaBlockEndLocation NULL");
            return;
        }

        arenaArea = functions.getBlocksBetweenPoints(
                this.arenaBlockStartLocation, this.arenaBlockEndLocation);
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
            totalLevels = 1;
        }

        this.averageLevel = (int) (totalLevels / currentPlayers.length);
        this.maxMobCounter = (int) (totalLevels / currentPlayers.length)
                + (currentPlayers.length * 5);

        return true;
    }

    public ArenaManager GetPlugin()
    {
        return plugin;
    }
}