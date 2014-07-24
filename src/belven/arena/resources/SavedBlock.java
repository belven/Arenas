package belven.arena.resources;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class SavedBlock
{
    public BlockState bs;
    public Location l;

    public SavedBlock(Block b)
    {
        l = b.getLocation();
        bs = b.getState();
    }
}
