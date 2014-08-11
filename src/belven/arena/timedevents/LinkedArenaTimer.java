package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;

public class LinkedArenaTimer extends BukkitRunnable
{
    ArenaBlock parentArena, childArena;
    ArenaManager plugin;

    public LinkedArenaTimer(ArenaBlock ParentArena, ArenaBlock ChildArena)
    {
        plugin = ParentArena.plugin;
        parentArena = ParentArena;
        childArena = ChildArena;
    }

    @Override
    public void run()
    {
        List<Player> players = new ArrayList<Player>();
        players.addAll(parentArena.arenaPlayers);

        for (Player p : players)
        {
            parentArena.arenaPlayers.remove(p);
            plugin.PlayersInArenas.remove(p);
            p.teleport(childArena.arenaWarp.getLocation());
        }

        childArena.Activate();
        this.cancel();
    }
}
