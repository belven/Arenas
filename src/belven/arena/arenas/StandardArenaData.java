package belven.arena.arenas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;

public abstract class StandardArenaData extends BaseArena {
	protected static final int _MaxMobCount = 25;
	protected BossMob bm = new BossMob();
	protected MobToMaterialCollecton MobToMat = new MobToMaterialCollecton();
	protected List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
	protected EliteMobCollection emc = new EliteMobCollection(this);
	protected int eliteWave = 0, maxMobCounter = 0;

	public StandardArenaData(Location startLocation, Location endLocation, String ArenaName, MobToMaterialCollecton mobToMat, ArenaManager Plugin, int TimerPeriod) {
		super(startLocation, endLocation, ArenaName, Plugin, TimerPeriod);
		setMobToMat(mobToMat);
		setType(ArenaTypes.Standard);
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

	public int getMaxMobCounter() {
		return maxMobCounter;
	}

	public void setMaxMobCounter(int maxMobCounter) {
		this.maxMobCounter = maxMobCounter;
	}

	public int getEliteWave() {
		return eliteWave;
	}

	public void setEliteWave(int eliteWave) {
		this.eliteWave = eliteWave;
	}

	public boolean isAir(Block b) {
		return b.getType() == Material.AIR;
	}

	public boolean isWithinArena(Block b) {
		return getArenaArea().contains(b);
	}

}