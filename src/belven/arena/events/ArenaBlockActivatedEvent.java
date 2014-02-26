package belven.arena.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaBlockActivatedEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private Location blockLocation;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public ArenaBlockActivatedEvent(Location blockLocation)
    {
        this.blockLocation = blockLocation;
    }

    public Location GetBlockLocation()
    {
        return blockLocation;
    }
}