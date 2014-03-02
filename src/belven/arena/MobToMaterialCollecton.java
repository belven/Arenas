package belven.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class MobToMaterialCollecton
{
    public List<MobToMaterial> MobToMaterials = new ArrayList<MobToMaterial>();

    public MobToMaterialCollecton()
    {

    }

    public void Add(MobToMaterial mtm)
    {
        this.MobToMaterials.add(mtm);
    }

    public String Add(EntityType et, Material m)
    {
        this.MobToMaterials.add(new MobToMaterial(et, m));
        return et.name() + " will now spawn on " + m.name();
    }

    public String Add(String et, String m)
    {
        this.MobToMaterials.add(new MobToMaterial(EntityType.valueOf(et),
                Material.valueOf(m)));
        return et + " will now spawn on " + m;
    }

    public List<EntityType> EntityTypes()
    {
        List<EntityType> ets = new ArrayList<EntityType>();

        for (MobToMaterial mtm : MobToMaterials)
        {
            ets.add(mtm.et);
        }
        return ets;
    }

    public List<Material> Materials()
    {
        List<Material> mats = new ArrayList<Material>();

        for (MobToMaterial mtm : MobToMaterials)
        {
            mats.add(mtm.m);
        }
        return mats;
    }

    public boolean Contains(Material m)
    {
        boolean tempContains = false;
        for (MobToMaterial mtm : MobToMaterials)
        {
            if (mtm.m == m)
            {
                tempContains = true;
                break;
            }
        }
        return tempContains;
    }

    public boolean Remove(MobToMaterial mtm)
    {
        if (MobToMaterials.contains(mtm))
        {
            MobToMaterials.remove(mtm);
            return true;
        }
        else
        {
            return false;
        }
    }

    public String Remove(String entityType, String material)
    {
        String hasRemoved = "Failed to remove";
        EntityType et = EntityType.valueOf(entityType);
        Material m = Material.valueOf(material);
        MobToMaterial mtom = new MobToMaterial(et, m);

        for (MobToMaterial mtm : MobToMaterials)
        {
            if (mtm.et == mtom.et && mtm.m == mtom.m)
            {
                MobToMaterials.remove(mtm);
                if (MobToMaterials.size() <= 0)
                {
                    return "Removed " + mtom.et + " " + mtom.m
                            + " no mobs left to spawn";
                }
                else
                {
                    hasRemoved = "Removed " + mtom.et + " " + mtom.m;
                }
                break;
            }
        }

        return hasRemoved;
    }

    public boolean Contains(EntityType et)
    {
        boolean tempContains = false;
        for (MobToMaterial mtm : MobToMaterials)
        {
            if (mtm.et == et)
            {
                tempContains = true;
                break;
            }
        }
        return tempContains;
    }

}
