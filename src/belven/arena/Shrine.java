package belven.arena;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Shrine {
	Block center, bottomCenter, bottomNorth, bottomSouth, bottomEast, bottomWest, bottomNorthEast, bottomNorthWest, bottomSouthWest, bottomSouthEast, top;

	public Shrine(Block b) {
		top = b;
		center = b.getRelative(BlockFace.DOWN);
		bottomCenter = center.getRelative(BlockFace.DOWN);
	}

	public void SetBaseBlocks() {
		bottomNorth = bottomCenter.getRelative(BlockFace.NORTH);
		bottomSouth = bottomCenter.getRelative(BlockFace.SOUTH);
		bottomEast = bottomCenter.getRelative(BlockFace.EAST);
		bottomWest = bottomCenter.getRelative(BlockFace.WEST);

		bottomNorthEast = bottomCenter.getRelative(BlockFace.NORTH_EAST);
		bottomNorthWest = bottomCenter.getRelative(BlockFace.NORTH_WEST);
		bottomSouthWest = bottomCenter.getRelative(BlockFace.SOUTH_WEST);
		bottomSouthEast = bottomCenter.getRelative(BlockFace.SOUTH_EAST);
	}
}
