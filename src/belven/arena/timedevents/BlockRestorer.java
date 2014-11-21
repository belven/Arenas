package belven.arena.timedevents;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockRestorer extends BukkitRunnable {
	BlockState origanalBlockMat;

	public BlockRestorer(Material matToChangeTo, Block blockToChange) {
		origanalBlockMat = blockToChange.getState();
		blockToChange.setType(matToChangeTo);
	}

	@Override
	public void run() {
		origanalBlockMat.update(true);
		this.cancel();
	}
}
