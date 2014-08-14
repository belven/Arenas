package belven.arena.timedevents;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockRestorer extends BukkitRunnable {
	Material origanalBlockMat;
	Block blockToChange;

	public BlockRestorer(Material matToChangeTo, Block blockToChange) {
		origanalBlockMat = blockToChange.getType();
		this.blockToChange = blockToChange;
		blockToChange.setType(matToChangeTo);
	}

	@Override
	public void run() {
		blockToChange.setType(origanalBlockMat);
		this.cancel();
	}
}
