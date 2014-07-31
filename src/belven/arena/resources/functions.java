package belven.arena.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;

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
                    if (e instanceof Player
                            && e.getLocation().distance(l) <= radius)
                    {
                        radiusEntities.add((Player) e);
                    }
                }
            }
        }

        return radiusEntities.toArray(new Player[radiusEntities.size()]);
    }

    @SuppressWarnings("deprecation")
    public static Player[] getNearbyPlayersNew(Location l, int radius)
    {
        HashSet<Entity> radiusEntities = new HashSet<Entity>();

        for (Player p : Bukkit.getServer().getOnlinePlayers())
        {
            if (p.getLocation().getWorld() == l.getWorld()
                    && p.getLocation().distance(l) <= radius)
            {
                radiusEntities.add(p);
            }
        }

        return radiusEntities.toArray(new Player[radiusEntities.size()]);
    }

    public static Location offsetLocation(Location l, double x, double y,
            double z)
    {
        return new Location(l.getWorld(), l.getX() + x, l.getY() + y, l.getZ()
                + z);
    }

    public static Location lookAt(Location loc, Location lookat)
    {
        loc = loc.clone();

        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        if (dx != 0)
        {
            if (dx < 0)
            {
                loc.setYaw((float) (1.5 * Math.PI));
            }
            else
            {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        }
        else if (dz < 0)
        {
            loc.setYaw((float) Math.PI);
        }

        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        loc.setPitch((float) -Math.atan(dy / dxz));
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
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

    public static void Heal(LivingEntity entityToHeal, int amountToHeal)
    {
        Damageable dEntityToHeal = (Damageable) entityToHeal;
        double max = dEntityToHeal.getMaxHealth();
        double current = dEntityToHeal.getHealth();

        if (entityToHeal != null)
        {
            for (int i = amountToHeal; i != 0; i--)
            {
                if ((current + i) < max)
                {
                    entityToHeal.setHealth(current + i);
                }
            }
        }
    }

    public static void RestoreHunger(Player entityToRestore, int amountToRestore)
    {
        if (entityToRestore != null)
        {
            for (int i = amountToRestore; i != 0; i--)
            {
                if ((entityToRestore.getFoodLevel() + i) < 10)
                {
                    entityToRestore.setFoodLevel(entityToRestore.getFoodLevel()
                            + i);
                }
            }
        }
    }

    public static void RestoreSaturation(Player entityToRestore,
            int amountToRestore)
    {
        if (entityToRestore != null)
        {
            for (int i = amountToRestore; i != 0; i--)
            {
                if ((entityToRestore.getSaturation() + i) < 10)
                {
                    entityToRestore.setSaturation(entityToRestore
                            .getSaturation() + i);
                }
            }
        }
    }

    public static List<Block> getBlocksBetweenPoints(Location min, Location max)
    {
        World w = min.getWorld();
        List<Block> tempList = new ArrayList<Block>();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x = x + 1)
        {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y = y + 1)
            {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z = z + 1)
                {
                    tempList.add(w.getBlockAt(x, y, z));
                }
            }
        }
        return tempList;
    }

    public static boolean isNotInteractiveBlock(Material material)
    {
        switch (material.toString())
        {
        case "CHEST":
        case "WORKBENCH":
        case "ANVIL":
        case "FURNACE":
        case "ENCHANTMENT_TABLE":
        case "ENDER_CHEST":
        case "BED":
        case "MINECART":
        case "SIGN":
        case "BUTTON":
        case "LEVER":
            return false;
        default:
            return true;
        }
    }

    public static ArrayList<ItemStack> getAllMeeleWeapons()
    {
        ArrayList<ItemStack> tempWeapons = new ArrayList<ItemStack>();
        tempWeapons.add(new ItemStack(Material.WOOD_SWORD));
        tempWeapons.add(new ItemStack(Material.STONE_SWORD));
        tempWeapons.add(new ItemStack(Material.IRON_SWORD));
        tempWeapons.add(new ItemStack(Material.GOLD_SWORD));
        tempWeapons.add(new ItemStack(Material.DIAMOND_SWORD));
        return tempWeapons;
    }

    public static boolean isAMeeleWeapon(Material material)
    {
        switch (material.toString())
        {
        case "WOOD_SWORD":
        case "STONE_SWORD":
        case "IRON_SWORD":
        case "GOLD_SWORD":
        case "DIAMOND_SWORD":
            return true;
        default:
            return false;
        }
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
