package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.Wave;
import belven.arena.blocks.ArenaBlock;
import belven.arena.resources.functions;

public class ArenaTimer extends BukkitRunnable
{
    private List<Block> arenaArea;
    private List<Player> arenaPlayers = new ArrayList<Player>();
    private List<Block> spawnArea = new ArrayList<Block>();
    private int currentRunTimes = 0;
    private ArenaBlock arenaBlock;
    private int averageLevel;
    private int maxMobCounter;
    @SuppressWarnings("unused")
    private Wave currentWave = null;

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
            arenaBlock.RemoveMobs();
            EndArena();
        }
        else if (!arenaBlock.isActive)
        {
            EndArena();
        }
        else if (currentRunTimes >= arenaBlock.maxRunTimes)
        {
            // if (arenaBlock.ArenaEntities.size() > 0)
            // {
            // new MessageTimer(arenaPlayers, "Arena " + arenaBlock.arenaName
            // + " has "
            // + String.valueOf(arenaBlock.ArenaEntities.size())
            // + " mobs left").run();
            // return;
            // }

            EndArena();
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

            currentWave = new Wave(spawnArea, arenaPlayers, arenaBlock,
                    maxMobCounter, currentRunTimes, averageLevel);
        }
    }

    public void EndArena()
    {

        new MessageTimer(arenaPlayers, "Arena " + arenaBlock.arenaName
                + " has ended!!").run();
        // RemoveMobs();
        new BlockRestorer(Material.REDSTONE_BLOCK, arenaBlock.deactivateBlock)
                .runTaskLater(arenaBlock.GetPlugin(),
                        functions.SecondsToTicks(4));
        arenaBlock.isActive = false;
        this.cancel();
    }

    public void GetArenaArea()
    {
        arenaArea = functions.getBlocksInRadius(
                arenaBlock.arenaBlockStartLocation.getLocation(),
                arenaBlock.radius);
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

    public boolean GetPlayers()
    {
        Location areaToCheck = arenaBlock.LocationToCheckForPlayers;
        Player[] currentPlayers = functions.getNearbyPlayers(areaToCheck,
                arenaBlock.radius + (arenaBlock.radius / 2));
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

            if (!arenaPlayers.contains(p))
            {
                arenaPlayers.add(p);
                arenaBlock.playersString = arenaBlock.playersString
                        + p.getName() + " ";
            }
        }

        if (arenaPlayers.size() <= 0)
        {
            return false;
        }

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

}
