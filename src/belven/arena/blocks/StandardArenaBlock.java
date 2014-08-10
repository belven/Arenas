package belven.arena.blocks;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import resources.EntityFunctions;
import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;

public class StandardArenaBlock extends ArenaBlock
{
    public StandardArenaBlock(Location startLocation, Location endLocation,
            String ArenaName, int Radius, MobToMaterialCollecton mobToMat,
            ArenaManager Plugin, int TimerPeriod)
    {
        super(startLocation, endLocation, ArenaName, Radius, mobToMat, Plugin,
                TimerPeriod);
    }

    @Override
    public void Activate()
    {
        if (arenaPlayers.size() == 0)
        {
            Player[] tempPlayers = EntityFunctions.getNearbyPlayersNew(
                    LocationToCheckForPlayers, (radius - 2) + (radius / 2));
            for (Player p : tempPlayers)
            {
                if (!plugin.IsPlayerInArena(p))
                {
                    plugin.WarpToArena(p, this);
                }
            }
        }

        if (arenaPlayers.size() != 0)
        {
            arenaRunID = UUID.randomUUID();
            isActive = true;
            ChallengeBlockWave = new Random().nextInt(maxRunTimes - 1) + 1;
            RemoveMobs();
            ArenaEntities.clear();
            GetArenaArea();
            GetSpawnArea();
            new ArenaTimer(this).runTaskLater(plugin, 10);
        }

    }

    @Override
    public void Deactivate()
    {
        arenaRunID = null;
        RestoreArena();
        isActive = false;
        RemoveMobs();
        ArenaEntities.clear();
    }

    @Override
    public void GoToNextWave()
    {
        if (arenaPlayers.size() > 0)
        {
            GetPlayersAverageLevel();
            currentRunTimes++;

            if (currentRunTimes == 1)
            {
                new MessageTimer(arenaPlayers, ArenaName() + " has Started!!")
                        .run();
            }

            if (currentRunTimes == ChallengeBlockWave)
            {
                currentChallengeBlock = ChallengeBlock.RandomChallengeBlock(
                        plugin, this);
            }

            new MessageTimer(arenaPlayers, ArenaName() + " Wave: "
                    + String.valueOf(currentRunTimes)).run();

            new Wave(this);

            new ArenaTimer(this).runTaskLater(plugin, timerPeriod);
        }

    }
}