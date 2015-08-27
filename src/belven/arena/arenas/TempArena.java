package belven.arena.arenas;

import org.bukkit.Location;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;

public class TempArena extends StandardArena {

	public TempArena(Location startLocation, Location endLocation, String ArenaName, int Radius, MobToMaterialCollecton mobToMat, ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, mobToMat, Plugin, TimerPeriod);
		setType(ArenaTypes.Temp);

		Activate();
	}

	@Override
	public void Deactivate() {
		super.Deactivate();
		getPlugin().currentArenaBlocks.remove(this);
	}
}