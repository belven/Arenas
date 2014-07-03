package belven.arena.timedevents;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.Wave;
import belven.arena.blocks.ArenaBlock;
import belven.arena.resources.functions;

public class ArenaTimer extends BukkitRunnable
{
    private ArenaBlock arenaBlock;
    @SuppressWarnings("unused")
    private Wave currentWave = null;

    public ArenaTimer(ArenaBlock arenaBlock)
    {
        this.arenaBlock = arenaBlock;
        arenaBlock.GetArenaArea();
        arenaBlock.GetSpawnArea();
    }

    @Override
    public void run()
    {
        CleanUpEntites();

        if (arenaBlock.arenaArea == null || arenaBlock.arenaArea.size() == 0
                || !arenaBlock.GetPlayers())
        {
            arenaBlock.RemoveMobs();
            EndArena();
        }
        else if (!arenaBlock.isActive)
        {
            this.cancel();
        }
        else if (arenaBlock.currentRunTimes >= arenaBlock.maxRunTimes)
        {
            if (arenaBlock.currentRunTimes >= (arenaBlock.maxRunTimes + 10))
            {
                new BlockRestorer(Material.REDSTONE_BLOCK,
                        arenaBlock.deactivateBlock).runTaskLater(
                        arenaBlock.GetPlugin(), functions.SecondsToTicks(1));
                // Give arena rewards
                arenaBlock.GiveRewards();
                EndArena();
            }
            else if (arenaBlock.ArenaEntities.size() > 0)
            {
                arenaBlock.currentRunTimes++;
                new MessageTimer(arenaBlock.arenaPlayers, "Arena "
                        + arenaBlock.arenaName + " has "
                        + String.valueOf(arenaBlock.ArenaEntities.size())
                        + " mobs left").run();
                new ArenaTimer(arenaBlock).runTaskLater(arenaBlock.plugin,
                        functions.SecondsToTicks(10));
                this.cancel();
                return;
            }
            else
            {
                new BlockRestorer(Material.REDSTONE_BLOCK,
                        arenaBlock.deactivateBlock).runTaskLater(
                        arenaBlock.GetPlugin(), functions.SecondsToTicks(1));
                arenaBlock.GiveRewards();
                EndArena();
            }
        }
        else if (arenaBlock.spawnArea.size() > 0
                && arenaBlock.currentRunTimes < arenaBlock.maxRunTimes)
        {
            arenaBlock.currentRunTimes++;
            if (arenaBlock.currentRunTimes == 1)
            {
                new MessageTimer(arenaBlock.arenaPlayers, arenaBlock.arenaName
                        + " has Started!!").run();
            }
            new MessageTimer(arenaBlock.arenaPlayers, arenaBlock.arenaName
                    + " Wave: " + String.valueOf(arenaBlock.currentRunTimes))
                    .run();

            currentWave = new Wave(arenaBlock);

            new ArenaTimer(arenaBlock).runTaskLater(arenaBlock.plugin,
                    arenaBlock.timerPeriod);
            this.cancel();
        }
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

    public void EndArena()
    {
        new MessageTimer(arenaBlock.arenaPlayers, "Arena "
                + arenaBlock.arenaName + " has ended!!").run();
        arenaBlock.RemoveMobs();
        arenaBlock.isActive = false;
        this.cancel();
    }

}
