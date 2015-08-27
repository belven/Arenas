package belven.arena.arenas;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

import belven.arena.ArenaManager;
import belven.arena.MobToMaterialCollecton;
import belven.arena.timedevents.ArenaTimer;

public class TempMazeArena extends StandardArena {
	private List<Block> blocksChanged = new ArrayList<>();

	public TempMazeArena(Location startLocation, Location endLocation, String ArenaName, int Radius, MobToMaterialCollecton mobToMat, ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, mobToMat, Plugin, TimerPeriod);
		setType(ArenaTypes.Temp);
		Activate();
	}

	@Override
	public void Activate() {
		blocksChanged.clear();
		SetPlayers();

		try {
			if (getArenaPlayers().size() != 0) {
				setState(ArenaState.Active);
				generateArenaMaze();

				setArenaRunID(UUID.randomUUID());
				GetSpawnArea();
				GenerateRandomPhases(0.5);

				for (LivingEntity le : getPlayers()) {
					Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(this);
					le.teleport(spawnLocation);
				}

				new ArenaTimer(this).runTaskLater(getPlugin(), 10);
			} else {
				getPlugin().writeToLog("Arena " + getName() + " was started but detected no players");
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			getPlugin().writeToLog("Arena " + getName() + " failed to go to " + ArenaState.Active.toString() + " state");
		}
	}

	private void generateArenaMaze() {
		getPlugin().writeToLog("Generate Arena Maze");

		World w = getArenaStartLocation().getWorld();
		Block cb = null;

		int startX = getArenaStartLocation().getBlockX();
		int endX = getArenaEndLocation().getBlockX();
		int startZ = getArenaStartLocation().getBlockZ();
		int endZ = getArenaEndLocation().getBlockZ();

		// Add 1 to Y becuase it starts at the ground level and we want to make
		// one from above ground
		int y = getArenaStartLocation().getBlockY() + 1;

		int largestX = startX > endX ? startX : endX;
		int largestZ = startZ > endZ ? startZ : endZ;

		int smallestX = startX < endX ? startX : endX;
		int smallestZ = startZ < endZ ? startZ : endZ;

		// Going from west to east until x is the same as the end location block

		for (int x = smallestX; x < largestX; x++) {
			// Going North to south until we reach the min block z
			for (int z = largestZ; z > smallestZ; z--) {
				cb = w.getBlockAt(x, y, z);
				Block blockBelow = cb.getRelative(BlockFace.DOWN);
				Material blockBelowType = blockBelow.getType();

				if (blockBelowType != Material.STATIONARY_WATER && blockBelowType != Material.STATIONARY_LAVA && blockBelowType != Material.WATER && blockBelowType != Material.LAVA) {
					// If this is the first row then just do a 50/50
					if (x == smallestX) {
						if (fithtyFithty() && !isAir(blockBelow)) {
							changeBlock(cb, blockBelowType);
						} else {
							// Set the block to air as we need to clear the
							// arena of being blocked
							changeBlock(cb, Material.AIR);
						}
					} else {
						// If the blocks west, westWest, westNorth and westSouth
						// contain something we might want to put air there
						if (getWestContainsBlock(cb)) {
							// If the north and northWest blocks are not air
							// then make this air, avoid blocking paths
							if (getNorthContainsBlock(cb)) {
								changeBlock(cb, Material.AIR);
								// Otherwise Have a chance to put a block their
							} else if (fithtyFithty() && !isAir(blockBelow)) {
								changeBlock(cb, blockBelowType);
							}
							// There are no blocks to the west so just have a
							// chance to put something there
						} else if (fithtyFithty() && !isAir(blockBelow)) {
							changeBlock(cb, blockBelowType);
						} else {
							// Set the block to air as we need to clear the
							// arena of being blocked
							changeBlock(cb, Material.AIR);
						}
					}
				}
			}
		}

		while (y < getArenaEndLocation().getBlockY()) {
			y++;

			for (Block b : blocksChanged) {
				cb = w.getBlockAt(b.getX(), y, b.getZ());
				cb.setType(b.getType());
			}
		}
	}

	public void changeBlock(Block b, Material m) {
		if (m == Material.GRASS || m == Material.DIRT) {
			m = Material.STONE;
		}

		b.setType(m);
		blocksChanged.add(b);
	}

	public boolean fithtyFithty() {
		return new Random().nextBoolean();
	}

	public boolean getNorthContainsBlock(Block b) {
		Block north = b.getRelative(BlockFace.NORTH);
		Block west = north.getRelative(BlockFace.WEST);
		return !isAir(west) || !isAir(north);
	}

	public boolean getWestContainsBlock(Block b) {
		Block west = b.getRelative(BlockFace.WEST);
		Block westWest = west.getRelative(BlockFace.WEST);
		Block westNorth = west.getRelative(BlockFace.NORTH);
		Block westSouth = west.getRelative(BlockFace.SOUTH);
		return !isAir(west) || !isAir(westWest) || !isAir(westNorth) || !isAir(westSouth);
	}

	@Override
	public boolean isAir(Block b) {
		return b.getType() == Material.AIR;
	}

	@Override
	public void Deactivate() {
		super.Deactivate();
		getPlugin().currentArenaBlocks.remove(this);
	}
}