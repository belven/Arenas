package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import belven.arena.blocks.ArenaBlock;
import belven.arena.events.ChallengeComplete;

public class Kills extends ChallengeType
{
    HashMap<EntityType, Integer> entitiesToKill = new HashMap<EntityType, Integer>();

    public Kills(HashMap<EntityType, Integer> entities)
    {
        challengeType = ChallengeTypes.Kills;
        entitiesToKill = entities;
    }

    public void EntityKilled(EntityType et)
    {
        if (!ChallengeComplete())
        {
            int amountLeft = entitiesToKill.get(et) != null ? entitiesToKill
                    .get(et) : 0;

            amountLeft--;
            entitiesToKill.put(et, amountLeft);

            if (ChallengeComplete())
            {
                Bukkit.getPluginManager()
                        .callEvent(new ChallengeComplete(this));
            }
        }
    }

    public List<String> ListRemainingEntities()
    {
        List<String> EntitiesLeft = new ArrayList<String>();
        for (EntityType et : entitiesToKill.keySet())
        {
            EntitiesLeft.add(et.name() + ": "
                    + String.valueOf(entitiesToKill.get(et)));
        }
        return EntitiesLeft;
    }

    @Override
    public boolean ChallengeComplete()
    {
        int enitiesLeft = 0;

        for (EntityType et : entitiesToKill.keySet())
        {
            enitiesLeft += entitiesToKill.get(et);
        }
        return enitiesLeft <= 0;
    }

    public static HashMap<EntityType, Integer> GetRandomEntities(ArenaBlock ab)
    {
        HashMap<EntityType, Integer> tempEntities = new HashMap<EntityType, Integer>();
        tempEntities.put(EntityType.ZOMBIE, 5);
        return tempEntities;
    }

}
