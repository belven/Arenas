package belven.arena;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class PlayerExtended
{
    public Player player;

    public PlayerExtended(Player p)
    {
        player = p;
    }

    public PlayerInventory GetInventory()
    {
        return player.getInventory();
    }

    public double GetHealth()
    {
        return ((Damageable) player).getHealth();
    }

    public double GetMaxHealth()
    {
        return ((Damageable) player).getMaxHealth();
    }

    public Block GetBlockBelow()
    {
        return player.getLocation().getBlock().getRelative(BlockFace.DOWN);
    }

}
