package belven.arena.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;
import belven.arena.resources.functions;

public class PlayerListener implements Listener
{
    private final ArenaManager plugin;

    public HashMap<String, Location> warpLocations = new HashMap<String, Location>();
    public ArrayList<String> playerDeathProtection = new ArrayList<String>();
    public HashMap<String, ItemStack[]> playerInventories = new HashMap<String, ItemStack[]>();

    public PlayerListener(ArenaManager instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event)
    {
        Sign currentSign;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().getType() == Material.SIGN)
            {
                currentSign = (Sign) event.getClickedBlock();

                if (currentSign.getLine(0) != null
                        && currentSign.getLine(0).contentEquals("[Arena]"))
                {
                    plugin.WarpToArena(event.getPlayer(),
                            currentSign.getLine(1));
                }
                else if (currentSign.getLine(0) != null
                        && currentSign.getLine(0).contentEquals("[ArenaLeave]"))
                {
                    plugin.LeaveArena(event.getPlayer());
                }
            }
            else if (event.getClickedBlock().getType() == Material.WALL_SIGN)
            {
                currentSign = (Sign) event.getClickedBlock().getState();

                if (currentSign.getLine(0) != null
                        && currentSign.getLine(0).contentEquals("[Arena]"))
                {
                    plugin.WarpToArena(event.getPlayer(),
                            currentSign.getLine(1));
                }
                else if (currentSign.getLine(0) != null
                        && currentSign.getLine(0).contentEquals("[ArenaLeave]"))
                {
                    plugin.LeaveArena(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event)
    {
        Player currentPlayer = (Player) event.getEntity();

        event.setNewLevel((int) (currentPlayer.getLevel()));

        if (plugin.currentArenaBlocks.size() > 0)
        {
            for (ArenaBlock ab : plugin.currentArenaBlocks)
            {
                if (ab.isActive
                        && ab.playersString.contains(currentPlayer.getName()))
                {
                    event.getDrops().clear();

                    PlayerInventory pi = currentPlayer.getInventory();

                    playerInventories.put(currentPlayer.getName(),
                            pi.getContents());

                    warpLocations.put(currentPlayer.getName(),
                            ab.arenaWarp.getLocation());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        Player currentPlayer = event.getPlayer();

        if (warpLocations.get(currentPlayer.getName()) != null)
        {
            event.setRespawnLocation(warpLocations.get(currentPlayer.getName()));

            currentPlayer.getInventory().setContents(
                    playerInventories.get(currentPlayer.getName()));

            currentPlayer.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, functions.SecondsToTicks(8), 4),
                    true);

            warpLocations.put(currentPlayer.getName(), null);
            playerInventories.put(currentPlayer.getName(), null);
            playerDeathProtection.add(currentPlayer.getName());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event)
    {
        if (event.getEntityType() == EntityType.PLAYER)
        {
            PlayerTakenDamage(event);
        }
    }

    public void PlayerTakenDamage(EntityDamageByEntityEvent event)
    {
        Player damagedPlayer = (Player) event.getEntity();

        if (playerDeathProtection.contains(damagedPlayer.getName()))
        {
            damagedPlayer.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, functions.SecondsToTicks(8), 4),
                    true);
            playerDeathProtection.remove(damagedPlayer.getName());
        }
    }

}
