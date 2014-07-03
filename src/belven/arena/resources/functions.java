package belven.arena.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    /*
     * public static UUID getPlayerUUIDByString(String playerName) {
     * 
     * @SuppressWarnings("deprecation") Player[] players =
     * Bukkit.getServer().getOnlinePlayers(); UUID tempUUID = null;
     * 
     * for(Player p : players) { if(p.getName() == playerName) { tempUUID =
     * p.getUniqueId() } }
     * 
     * return tempUUID; }
     */
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
            return false;
        case "WORKBENCH":
            return false;
        case "ANVIL":
            return false;
        case "FURNACE":
            return false;
        case "ENCHANTMENT_TABLE":
            return false;
        case "ENDER_CHEST":
            return false;
        case "BED":
            return false;
        case "MINECART":
            return false;
        case "SIGN":
            return false;
        case "BUTTON":
            return false;
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
            return true;
        case "STONE_SWORD":
            return true;
        case "IRON_SWORD":
            return true;
        case "GOLD_SWORD":
            return true;
        case "DIAMOND_SWORD":
            return true;
        default:
            return false;
        }
    }

    public static boolean IsAMob(EntityType currentEntityType)
    {
        if (currentEntityType == EntityType.BLAZE
                || currentEntityType == EntityType.CAVE_SPIDER
                || currentEntityType == EntityType.CREEPER
                || currentEntityType == EntityType.ENDER_DRAGON
                || currentEntityType == EntityType.ENDERMAN
                || currentEntityType == EntityType.GHAST
                || currentEntityType == EntityType.MAGMA_CUBE
                || currentEntityType == EntityType.PIG_ZOMBIE
                || currentEntityType == EntityType.SKELETON
                || currentEntityType == EntityType.SPIDER
                || currentEntityType == EntityType.SLIME
                || currentEntityType == EntityType.WITCH
                || currentEntityType == EntityType.WITHER
                || currentEntityType == EntityType.ZOMBIE)
        {
            return true;
        }
        else
            return false;
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
