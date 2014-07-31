package belven.arena;

import org.bukkit.inventory.ItemStack;

public class Gear
{
    public ItemStack h;
    public ItemStack c;
    public ItemStack l;
    public ItemStack b;
    public ItemStack w;

    public Gear()
    {

    }

    public Gear(ItemStack helmet, ItemStack chest, ItemStack legs,
            ItemStack boots, ItemStack weapon)
    {
        h = helmet;
        c = chest;
        l = legs;
        b = boots;
        w = weapon;
    }

    public void Clear()
    {
        h = null;
        c = null;
        l = null;
        b = null;
        w = null;
    }

}
