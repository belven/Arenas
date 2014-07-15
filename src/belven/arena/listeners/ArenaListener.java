package belven.arena.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;
import belven.arena.events.ArenaBlockActivatedEvent;

public class ArenaListener implements Listener
{
    public ArenaManager plugin;

    public ArenaListener(ArenaManager instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onArenaBlockActivatedEvent(ArenaBlockActivatedEvent event)
    {
        if (plugin.currentArenaBlocks.size() > 0)
        {
            for (ArenaBlock ab : plugin.currentArenaBlocks)
            {
                if (event.GetBlockLocation().equals(
                        ab.blockToActivate.getLocation())
                        && !ab.isActive)
                {
                    ab.Activate();
                }
            }
        }
    }

    @EventHandler
    public void onArenaBlockBreakEvent(BlockBreakEvent event)
    {
        if (plugin.IsPlayerInArena(event.getPlayer()))
        {
            if (plugin.getArenaInIsPlayer(event.getPlayer()).isActive)
            {
                event.setCancelled(true);
            }
        }
    }
}
