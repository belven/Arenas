package belven.arena.timedevents;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.blocks.ArenaBlock;

public class LinkedArenaTimer extends BukkitRunnable
{
    ArenaBlock parentArena, childArena;

    public LinkedArenaTimer(ArenaBlock ParentArena, ArenaBlock ChildArena)
    {
        parentArena = ParentArena;
        childArena = ChildArena;
    }

    @Override
    public void run()
    {
        for (Player p : parentArena.arenaPlayers)
        {
            p.teleport(childArena.arenaWarp.getLocation());
            childArena.arenaPlayers.add(p);
        }

        parentArena.arenaPlayers.clear();
        childArena.Activate();
        this.cancel();
    }
}
