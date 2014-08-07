package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.EntityType;

import belven.arena.rewardclasses.ExperienceReward;
import belven.arena.rewardclasses.Reward;

public class Kills extends ChallengeType
{
    HashMap<EntityType, Integer> entitiesToKill = new HashMap<EntityType, Integer>();
    Reward killsReward = new ExperienceReward(10);

    public Kills(HashMap<EntityType, Integer> entities, Reward reward)
    {
        challengeType = Type.Kills;
        entitiesToKill = entities;
        killsReward = reward;
    }

    public void EntityKilled(EntityType et)
    {
        int amountLeft = entitiesToKill.get(et);
        amountLeft--;
        entitiesToKill.put(et, amountLeft);
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
}
