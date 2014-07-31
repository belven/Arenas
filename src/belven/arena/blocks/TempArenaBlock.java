package belven.arena.blocks;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.resources.functions;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;

public class TempArenaBlock extends ArenaBlock
{

    public TempArenaBlock(Location startLocation, Location endLocation,
            String ArenaName, int Radius, MobToMaterialCollecton mobToMat,
            ArenaManager Plugin, int TimerPeriod)
    {
        super(startLocation, endLocation, ArenaName, Radius, mobToMat, Plugin,
                TimerPeriod);

        plugin.currentArenaBlocks.add(this);
        Activate();
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
                    plugin.WarpToArena(p, this);
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
        plugin.currentArenaBlocks.remove(this);
    }

    public void GoToNextWave()
    {
        if (arenaPlayers.size() > 0)
        {
            GetPlayersAverageLevel();
            currentRunTimes++;

            if (currentRunTimes == 1)
            {
                new MessageTimer(arenaPlayers, ChatColor.RED + arenaName
                        + ChatColor.WHITE + " has Started!!").run();
            }

            new MessageTimer(arenaPlayers, ChatColor.RED + arenaName
                    + ChatColor.WHITE + " Wave: "
                    + String.valueOf(currentRunTimes)).run();

            new Wave(this);
            new ArenaTimer(this).runTaskLater(plugin, timerPeriod);
        }
    }

}