package belven.arena.timedevents;

import org.bukkit.Material;
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
        arenaBlock.currentRunTimes++;
        if (!arenaBlock.GetPlayers())
        {
            arenaBlock.RemoveMobs();
            EndArena();
        }
        else if (!arenaBlock.isActive)
        {
            EndArena();
        }
        else if (arenaBlock.currentRunTimes >= arenaBlock.maxRunTimes)
        {
            if (arenaBlock.currentRunTimes >= (arenaBlock.maxRunTimes + 10))
            {
                new BlockRestorer(Material.REDSTONE_BLOCK,
                        arenaBlock.deactivateBlock).runTaskLater(
                        arenaBlock.GetPlugin(), functions.SecondsToTicks(1));
                EndArena();
            }
            else if (arenaBlock.ArenaEntities.size() > 0)
            {
                new MessageTimer(arenaBlock.arenaPlayers, "Arena "
                        + arenaBlock.arenaName + " has "
                        + String.valueOf(arenaBlock.ArenaEntities.size())
                        + " mobs left").run();
                new ArenaTimer(arenaBlock).runTaskLater(arenaBlock.plugin,
                        functions.SecondsToTicks(5));
                this.cancel();
                return;
            }
            else
            {
                new BlockRestorer(Material.REDSTONE_BLOCK,
                        arenaBlock.deactivateBlock).runTaskLater(
                        arenaBlock.GetPlugin(), functions.SecondsToTicks(1));
                EndArena();
            }
        }
        else if (arenaBlock.spawnArea.size() > 0
                && arenaBlock.currentRunTimes < arenaBlock.maxRunTimes)
        {
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

    public void EndArena()
    {
        new MessageTimer(arenaBlock.arenaPlayers, "Arena "
                + arenaBlock.arenaName + " has ended!!").run();
        arenaBlock.RemoveMobs();
        arenaBlock.isActive = false;
        this.cancel();
    }

}
