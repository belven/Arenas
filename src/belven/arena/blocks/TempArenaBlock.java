package belven.arena.blocks;

import org.bukkit.Location;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;

public class TempArenaBlock extends StandardArenaBlock {

	public TempArenaBlock(Location startLocation, Location endLocation,
			String ArenaName, int Radius, MobToMaterialCollecton mobToMat,
			ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Radius, mobToMat, Plugin,
				TimerPeriod);

		Activate();
		type = ArenaTypes.Temp;
	}

	public void Deactivate() {
		super.Deactivate();
		plugin.currentArenaBlocks.remove(this);
	}
}