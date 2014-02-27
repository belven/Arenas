package belven.arena.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

public class functions
{
    public static Player[] getNearbyPlayers(Location l, int radius)
    {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Entity> radiusEntities = new HashSet<Entity>();

        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
        {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++)
            {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();

                for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z
                        + (chZ * 16)).getChunk().getEntities())
                {
                    if (e.getLocation().distance(l) <= radius
                            && e instanceof Player
                            && e.getLocation().getBlock() != l.getBlock())
                    {
                        radiusEntities.add((Player) e);
                    }
                }
            }
        }

        return radiusEntities.toArray(new Player[radiusEntities.size()]);
    }
    
    public static List<Block> getBlocksInRadius(Location l, int radius)
    {
        World w = l.getWorld();
        int xCoord = (int) l.getX();
        int zCoord = (int) l.getZ();
        int YCoord = (int) l.getY();

        List<Block> tempList = new ArrayList<Block>();

        for (int x = 0; x <= 2 * radius; x++)
        {
            for (int z = 0; z <= 2 * radius; z++)
            {
                for (int y = 0; y <= 2 * radius; y++)
                {
                    tempList.add(w.getBlockAt(xCoord + x, YCoord + y, zCoord
                            + z));
                }
            }
        }
        return tempList;
    }

    public static int SecondsToTicks(int seconds)
    {
        return (seconds * 20);
    }
    
    public static int MobMaxHealth(LivingEntity entity)
    {
        if (entity.getType() == EntityType.ZOMBIE)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.SKELETON)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.SPIDER)
        {
            return 16;
        }
        else if (entity.getType() == EntityType.CREEPER)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.WITHER)
        {
            return 300;
        }
        else if (entity.getType() == EntityType.BLAZE)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.ENDERMAN)
        {
            return 40;
        }
        else if (entity.getType() == EntityType.CAVE_SPIDER)
        {
            return 12;
        }
        else if (entity.getType() == EntityType.GHAST)
        {
            return 10;
        }
        else if (entity.getType() == EntityType.MAGMA_CUBE)
        {
            MagmaCube MagmaCube = (MagmaCube) entity;

            if (MagmaCube.getSize() == 4)

            {
                return 16;
            }
            else if (MagmaCube.getSize() == 2)
            {
                return 4;
            }
            else
            {
                return 1;
            }
        }
        else if (entity.getType() == EntityType.PIG_ZOMBIE)
        {
            return 20;
        }
        else if (entity.getType() == EntityType.SLIME)
        {
            Slime slime = (Slime) entity;

            if (slime.getSize() == 4)

            {
                return 16;
            }
            else if (slime.getSize() == 2)
            {
                return 4;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return 20;
        }
    }
}
