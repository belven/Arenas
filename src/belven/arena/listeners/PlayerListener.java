package belven.arena.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;

public class PlayerListener implements Listener
{
    private final ArenaManager plugin;

    Location arenaWarp;
    ItemStack[] currentPlayerInventory;

    public PlayerListener(ArenaManager instance)
    {
        plugin = instance;
    }  

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event)
    {
        Player currentPlayer = (Player) event.getEntity();

        event.setNewLevel(currentPlayer.getLevel() / 2);

        if (plugin.currentArenaBlocks.size() > 0)
        {
            for (ArenaBlock ab : plugin.currentArenaBlocks)
            {
                if (ab.isActive
                        && ab.playersString.contains(currentPlayer.getName()))
                {
                    List<ItemStack> drops = event.getDrops();
                    event.getDrops().clear();
                    drops.clear();

                    PlayerInventory pi = currentPlayer.getInventory();

                    currentPlayerInventory = currentPlayer.getInventory()
                            .getContents();

                    drops.add(pi.getChestplate());
                    drops.add(pi.getLeggings());
                    drops.add(pi.getHelmet());
                    drops.add(pi.getBoots());
                    drops.add(pi.getItemInHand());
                    event.getDrops().addAll(drops);

                    arenaWarp = ab.arenaWarp.getLocation();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        Player currentPlayer = event.getPlayer();

        if (arenaWarp != null)
        {
            event.setRespawnLocation(arenaWarp);
            currentPlayer.getInventory().setContents(currentPlayerInventory);
            arenaWarp = null;
            currentPlayerInventory = null;
        }
    }
    
    public boolean isNotInteractiveBlock(Material material)
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

    public boolean IsAMob(EntityType currentEntityType)
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

    public int SecondsToTicks(int seconds)
    {
        return seconds * 20;
    }
}
