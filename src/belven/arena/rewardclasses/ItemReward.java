package belven.arena.rewardclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class ItemReward extends Reward
{
    List<ItemStack> rewards = new ArrayList<ItemStack>();

    public ItemReward(List<ItemStack> items)
    {
        rewardType = Type.Items;
        rewards = items;
    }
}
