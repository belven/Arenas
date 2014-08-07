package belven.arena.rewardclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemReward extends Reward
{
    public List<ItemStack> rewards = new ArrayList<ItemStack>();

    public ItemReward(List<ItemStack> items)
    {
        rewardType = RewardType.Items;
        rewards = items;
    }

    public static List<ItemStack> RandomItemRewards()
    {
        List<ItemStack> items = new ArrayList<ItemStack>();
        items.add(new ItemStack(Material.DIAMOND));
        return items;
    }
}
