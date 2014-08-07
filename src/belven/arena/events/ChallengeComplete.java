package belven.arena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import belven.arena.challengeclasses.ChallengeType;

public class ChallengeComplete extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private ChallengeType ct;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public ChallengeComplete(ChallengeType ct)
    {
        this.ct = ct;
    }

    public ChallengeType GetChallengeType()
    {
        return ct;
    }
}