package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import resources.Functions;
import belven.arena.blocks.ArenaBlock;
import belven.arena.events.ArenaSuccessful;

public class ArenaTimer extends BukkitRunnable
{
    private ArenaBlock arenaBlock;

    public UUID arenaRunID;
    public int nextWave = 0;

    private Random randomGenerator = new Random();

    public ArenaTimer(ArenaBlock arenaBlock)
    {
        this.arenaBlock = arenaBlock;
        arenaRunID = arenaBlock.arenaRunID;
        nextWave = arenaBlock.currentRunTimes;
    }

    @Override
    public void run()
    {
        if (arenaRunID != arenaBlock.arenaRunID
                || nextWave < arenaBlock.currentRunTimes)
        {
            this.cancel();
        }

        CleanUpEntites();

        if (arenaBlock.arenaArea.size() == 0
                || arenaBlock.arenaPlayers.size() == 0)
        {
            EndArena();
        }
        else if (!arenaBlock.isActive)
        {
            arenaBlock.RemoveMobs();
            this.cancel();
        }
        // arena beyond last wave
        else if (arenaBlock.currentRunTimes >= arenaBlock.maxRunTimes)
        {
            // we have exceeded the amount of times the arena can run for
            if (arenaBlock.ArenaEntities.size() == 0)
            {
                ArenaSuccessfull();
            }
            else if (arenaBlock.currentRunTimes >= arenaBlock.maxRunTimes
                    && arenaBlock.currentRunTimes % 10 == 0)
            {
                SpreadEntities();
                ArenaHasEntitiesLeft();
            }
            else if (arenaBlock.ArenaEntities.size() > 0)
            {
                ArenaHasEntitiesLeft();
            }
            else
            {
                ArenaSuccessfull();
            }
        }
        else if (arenaBlock.spawnArea.size() > 0
                && arenaBlock.currentRunTimes < arenaBlock.maxRunTimes)
        {
            arenaBlock.GoToNextWave();
            this.cancel();
        }
    }

    private void SpreadEntities()
    {
        for (LivingEntity le : arenaBlock.ArenaEntities)
        {
            int randomInt = randomGenerator
                    .nextInt(arenaBlock.spawnArea.size());
            Location spawnLocation = arenaBlock.spawnArea.get(randomInt)
                    .getLocation();

            if (spawnLocation != null)
            {
                spawnLocation = new Location(spawnLocation.getWorld(),
                        spawnLocation.getX() + 0.5, spawnLocation.getY(),
                        spawnLocation.getZ() + 0.5);
                le.teleport(spawnLocation);
            }
        }

        new MessageTimer(arenaBlock.arenaPlayers, ChatColor.RED
                + "Scrambling Mobs").run();
    }

    private void ArenaHasEntitiesLeft()
    {
        arenaBlock.GetPlayersAverageLevel();

        arenaBlock.currentRunTimes++;

        new MessageTimer(arenaBlock.arenaPlayers, "Arena "
                + arenaBlock.ArenaName() + " has "
                + String.valueOf(arenaBlock.ArenaEntities.size())
                + " mobs left").run();

        new ArenaTimer(arenaBlock).runTaskLater(arenaBlock.plugin,
                Functions.SecondsToTicks(10));

        this.cancel();
    }

    private void CleanUpEntites()
    {
        Iterator<LivingEntity> ArenaEntities = arenaBlock.ArenaEntities
                .iterator();

        while (ArenaEntities.hasNext())
        {
            LivingEntity le = ArenaEntities.next();
            if (le != null && le.isDead())
            {
                if (le.hasMetadata("ArenaMob"))
                {
                    le.removeMetadata("ArenaMob", arenaBlock.plugin);
                }
                ArenaEntities.remove();
            }
        }
    }

    public void ArenaSuccessfull()
    {
        new BlockRestorer(Material.REDSTONE_BLOCK, arenaBlock.deactivateBlock)
                .runTaskLater(arenaBlock.plugin, Functions.SecondsToTicks(1));
        // Give arena rewards
        arenaBlock.GiveRewards();

        // check to see if we need to run other linked arenas
        if (arenaBlock.linkedArenas.size() > 0)
        {
            for (ArenaBlock lab : arenaBlock.linkedArenas)
            {
                if (lab != null && !lab.isActive)
                {
                    new LinkedArenaTimer(arenaBlock, lab)
                            .runTaskLater(
                                    arenaBlock.plugin,
                                    Functions
                                            .SecondsToTicks(arenaBlock.linkedArenaDelay));
                }
            }
        }

        Bukkit.getPluginManager().callEvent(new ArenaSuccessful(arenaBlock));

        EndArena();
    }

    public void EndArena()
    {
        new MessageTimer(arenaBlock.arenaPlayers, "Arena "
                + arenaBlock.ArenaName() + " has ended!!").run();
        arenaBlock.RemoveMobs();
        arenaBlock.isActive = false;

        if (arenaBlock.linkedArenas.size() == 0)
        {
            List<Player> ArenaPlayers = new ArrayList<Player>();
            ArenaPlayers.addAll(arenaBlock.arenaPlayers);

            for (Player p : ArenaPlayers)
            {
                arenaBlock.plugin.LeaveArena(p);
            }

        }
        this.cancel();
    }

}
