package belven.arena.arenas;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;
import belven.arena.Wave;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.timedevents.ArenaTimer;
import belven.arena.timedevents.MessageTimer;
import belven.resources.Functions;

public class StandardArena extends BaseArena {

	protected BossMob bm = new BossMob();
	protected MobToMaterialCollecton MobToMat = new MobToMaterialCollecton();
	protected List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
	protected EliteMobCollection emc = new EliteMobCollection(this);

	public StandardArena(Location startLocation, Location endLocation, String ArenaName, int Radius,
			MobToMaterialCollecton mobToMat, ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Plugin, TimerPeriod);
		MobToMat = mobToMat;
		setType(ArenaTypes.Standard);
	}

	public void GetSpawnArea() {
		Location spawnLocation;
		getSpawnArea().clear();

		List<Block> tempSpawnArea = Functions.getBlocksBetweenPoints(getSpawnArenaStartLocation(),
				getSpawnArenaEndLocation());

		if (tempSpawnArea != null && tempSpawnArea.size() > 0) {
			for (Block b : tempSpawnArea) {
				spawnLocation = b.getLocation();
				spawnLocation = CanSpawnAt(spawnLocation);
				if (spawnLocation != null && !b.equals(getSpawnArenaStartLocation())) {
					Block spawnBlock = spawnLocation.getBlock();
					getSpawnArea().add(spawnBlock);
				}
			}
		}
	}

	@Override
	public void Activate() {
		SetPlayers();

		if (getArenaPlayers().size() != 0) {
			setArenaRunID(UUID.randomUUID());
			setActive(true);
			setChallengeBlockWave(new Random().nextInt(getMaxRunTimes()));

			if (getChallengeBlockWave() <= 0) {
				setChallengeBlockWave(1);
			}

			RemoveMobs();
			ArenaEntities.clear();
			GetSpawnArea();
			new ArenaTimer(this).runTaskLater(getPlugin(), 10);
		}
	}

	public BossMob getBossMob() {
		return bm;
	}

	public void setBossMob(BossMob bm) {
		this.bm = bm;
	}

	public MobToMaterialCollecton getMobToMat() {
		return MobToMat;
	}

	public void setMobToMat(MobToMaterialCollecton mobToMat) {
		MobToMat = mobToMat;
	}

	public List<LivingEntity> getArenaEntities() {
		return ArenaEntities;
	}

	public void setArenaEntities(List<LivingEntity> arenaEntities) {
		ArenaEntities = arenaEntities;
	}

	public EliteMobCollection getEliteMobCollection() {
		return emc;
	}

	public void setEliteMobCollection(EliteMobCollection emc) {
		this.emc = emc;
	}

	private Location CanSpawnAt(Location currentLocation) {
		Block currentBlock = currentLocation.getBlock();
		Block blockBelow = currentBlock.getRelative(BlockFace.DOWN);
		Block blockAbove = currentBlock.getRelative(BlockFace.UP);

		if (currentBlock.getType() == Material.AIR && blockAbove.getType() == Material.AIR
				&& MobToMat.Contains(blockBelow.getType())) {
			return currentLocation;
		} else {
			return null;
		}
	}

	public void RemoveMobs() {
		setCurrentRunTimes(0);
		for (LivingEntity le : ArenaEntities) {
			if (!le.isDead()) {
				le.removeMetadata("ArenaMob", getPlugin());
				le.setHealth(0.0);
			}
		}
	}

	@Override
	public void Deactivate() {
		setArenaRunID(null);
		RestoreArena();
		setActive(false);
		RemoveMobs();
		ArenaEntities.clear();

		if (getCurrentChallengeBlock() != null) {
			getCurrentChallengeBlock().challengeBlockState.update(true);
		}
		ListIterator<Player> players = getArenaPlayers().listIterator();

		while (players.hasNext()) {
			Player p = players.next();
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	@Override
	public void GoToNextWave() {
		if (getArenaPlayers().size() > 0) {
			GetPlayersAverageLevel();
			setCurrentRunTimes(getCurrentRunTimes() + 1);

			if (getCurrentRunTimes() == 1) {
				new MessageTimer(getArenaPlayers(), ArenaName() + " has Started!!").run();
			}

			if (getCurrentRunTimes() == getChallengeBlockWave()) {
				setCurrentChallengeBlock(ChallengeBlock.RandomChallengeBlock(getPlugin(), this));
			}

			new MessageTimer(getArenaPlayers(), ArenaName() + " Wave: " + String.valueOf(getCurrentRunTimes())).run();
			new Wave(this);
			new ArenaTimer(this).runTaskLater(getPlugin(), getTimerPeriod());
		}
	}
}