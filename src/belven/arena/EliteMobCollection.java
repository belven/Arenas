package belven.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.PlayerInventory;

import belven.arena.blocks.ArenaBlock;

public class EliteMobCollection
{
    public ArenaBlock ab;
    public List<EliteMob> ems = new ArrayList<EliteMob>();

    public EliteMobCollection(ArenaBlock ArenaBlock)
    {
        ab = ArenaBlock;
    }

    public String Set(EntityType et, PlayerInventory pi)
    {
        EliteMob em;

        if (Contains(et))
        {
            em = Get(et);
            em.armor.Clear();
            em.armor.h = pi.getHelmet();
            em.armor.c = pi.getChestplate();
            em.armor.l = pi.getLeggings();
            em.armor.b = pi.getBoots();
            em.armor.w = pi.getItemInHand();
        }
        else
        {
            em = new EliteMob();
            em.type = et;
            em.armor = new Gear(pi.getHelmet(), pi.getChestplate(),
                    pi.getLeggings(), pi.getBoots(), pi.getItemInHand());
            this.ems.add(em);
        }

        return "Elite mob " + et.name() + " was given your gear";
    }

    public String Remove(EntityType et)
    {
        String removeString = "Failed to remove entity";

        if (Contains(et))
        {
            EliteMob em = Get(et);
            this.ems.remove(em);

            if (ems.size() == 0)
            {
                return "Elite mob " + et.name()
                        + " was removed. No elite mobs left!!";
            }
            else
            {
                return "Elite mob " + et.name() + " was removed.";
            }
        }

        return removeString;
    }

    public EliteMob Get(EntityType et)
    {
        for (EliteMob em : ems)
        {
            if (em.type == et)
            {
                return em;
            }
        }

        return null;
    }

    public boolean Contains(EntityType et)
    {
        for (EliteMob em : ems)
        {
            if (em.type == et)
            {
                return true;
            }
        }
        return false;
    }
}
