package belven.arena.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import belven.arena.ArenaManager;
import belven.arena.events.ArenaBlockActivatedEvent;

public class BlockListener implements Listener
{
    public ArenaManager plugin;

    public BlockListener(ArenaManager instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onBlockRedstoneEvent(BlockRedstoneEvent event)
    {
        Block tempblock = event.getBlock();

        Bukkit.getPluginManager().callEvent(
                new ArenaBlockActivatedEvent(tempblock.getLocation()));
        return;
    }

    @EventHandler
    public void onBlockBurnEvent(BlockBurnEvent event)
    {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event)
    {
        event.setCancelled(true);
    }
}
