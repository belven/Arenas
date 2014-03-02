package belven.arena.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import belven.arena.ArenaManager;
import belven.arena.BossMob;
import belven.arena.EliteMobCollection;
import belven.arena.MobToMaterialCollecton;
import belven.arena.timedevents.ArenaTimer;

public class ArenaBlock
{
    private ArenaManager plugin;
    public boolean isActive = false;

    public String arenaName, playersString;
    public Block blockToActivate, deactivateBlock, arenaWarp,
            arenaBlockStartLocation;

    public Location LocationToCheckForPlayers;

    public int radius, maxRunTimes, timerDelay, timerPeriod, eliteWave;

    public BossMob bm = new BossMob();
    public MobToMaterialCollecton MobToMat;
    public List<LivingEntity> ArenaEntities = new ArrayList<LivingEntity>();
    public EliteMobCollection emc = new EliteMobCollection(this);

    public ArenaBlock(Block block, String ArenaName, Integer radius,
            MobToMaterialCollecton mobToMat, ArenaManager plugin,
            int timerDelay, int timerPeriod)
    {
        this.arenaBlockStartLocation = block.getWorld().getBlockAt(
                new Location(block.getWorld(), block.getX(), block.getY() - 1,
                        block.getZ()));

        this.blockToActivate = block;

        this.deactivateBlock = block.getWorld().getBlockAt(
                new Location(block.getWorld(), block.getX(), block.getY() + 2,
                        block.getZ()));

        this.LocationToCheckForPlayers = blockToActivate.getLocation();
        this.arenaWarp = block;
        this.radius = radius;
        this.MobToMat = mobToMat;
        this.timerDelay = timerDelay;
        this.timerPeriod = timerPeriod;
        this.arenaName = ArenaName;
        this.plugin = plugin;
        this.maxRunTimes = 5;
    }

    public void Activate()
    {
        isActive = true;
        ArenaEntities.clear();
        new ArenaTimer(this).runTaskTimer(plugin, timerDelay, timerPeriod);
    }

    public void Deactivate()
    {
        isActive = false;
        RemoveMobs();
        ArenaEntities.clear();
    }

    public void RemoveMobs()
    {
        for (LivingEntity le : ArenaEntities)
        {
            if (!le.isDead())
            {
                le.removeMetadata("ArenaMob", GetPlugin());
                le.setHealth(0);
            }
        }
    }

    public ArenaManager GetPlugin()
    {
        return plugin;
    }
}