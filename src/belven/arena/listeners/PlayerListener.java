package belven.arena.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;
import belven.arena.blocks.ChallengeBlock;
import belven.arena.challengeclasses.ChallengeType.ChallengeTypes;
import belven.arena.challengeclasses.PlayerSacrifice;

public class PlayerListener implements Listener
{
    private final ArenaManager plugin;
    Random randomGenerator = new Random();

    public HashMap<String, Location> warpLocations = new HashMap<String, Location>();
    public ArrayList<String> playerDeathProtection = new ArrayList<String>();
    public HashMap<String, ItemStack[]> playerInventories = new HashMap<String, ItemStack[]>();
    public HashMap<String, ItemStack[]> playerArmour = new HashMap<String, ItemStack[]>();
    public HashMap<String, Collection<PotionEffect>> playerEffects = new HashMap<String, Collection<PotionEffect>>();

    public PlayerListener(ArenaManager instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        if (plugin.IsPlayerInArena(event.getPlayer()))
        {
            plugin.LeaveArena(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event)
    {
        if (plugin != null && plugin.IsPlayerInArena(event.getPlayer())
                && plugin.getArenaInIsPlayer(event.getPlayer()).isActive)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event)
    {
        if (plugin.IsPlayerInArena(event.getPlayer()))
        {
            ArenaBlock ab = plugin.getArenaInIsPlayer(event.getPlayer());

            if (event.getTo().getWorld() == ab.LocationToCheckForPlayers
                    .getWorld())
            {
                if (event.getTo().distance(ab.LocationToCheckForPlayers) > (ab.radius * 2))
                {
                    plugin.LeaveArena(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerVelocityEvent(PlayerVelocityEvent event)
    {
        if (event.getPlayer().isBlocking())
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event)
    {
        Sign currentSign;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().hasMetadata("Challenge Block"))
            {
                MetadataValue data = event.getClickedBlock()
                        .getMetadata("Challenge Block").get(0);
                ChallengeBlock cb = (ChallengeBlock) data.value();

                if (cb.challengeType.type == ChallengeTypes.PlayerSacrifice)
                {
                    PlayerSacrifice ps = (PlayerSacrifice) cb.challengeType;
                    ps.SacrificePlayer(event.getPlayer());
                }
                else if (cb.challengeType.type == ChallengeTypes.ItemSacrifice)
                {
                    // TO DO
                }

            }
            else if (event.getClickedBlock().getType() == Material.SIGN)
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
        event.getDrops().clear();

        event.setNewLevel(currentPlayer.getLevel());

        if (plugin.IsPlayerInArena(currentPlayer))
        {
            ArenaBlock ab = plugin.getArenaInIsPlayer(currentPlayer);
            int randomInt = randomGenerator.nextInt(ab.spawnArea.size());
            Location spawnLocation = ab.spawnArea.get(randomInt).getLocation();
            warpLocations.put(currentPlayer.getName(), spawnLocation);
            playerEffects.put(currentPlayer.getName(),
                    currentPlayer.getActivePotionEffects());
        }

        PlayerInventory pi = currentPlayer.getInventory();
        playerInventories.put(currentPlayer.getName(), pi.getContents());
        playerArmour.put(currentPlayer.getName(), currentPlayer.getInventory()
                .getArmorContents());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        Player currentPlayer = event.getPlayer();

        if (warpLocations.get(currentPlayer.getName()) != null)
        {
            Location spawnLocation = warpLocations.get(currentPlayer.getName());

            if (spawnLocation != null)
            {
                spawnLocation = Functions.offsetLocation(spawnLocation, 0.5, 0,
                        0.5);

                event.setRespawnLocation(spawnLocation);
            }

            warpLocations.put(currentPlayer.getName(), null);
            playerDeathProtection.add(currentPlayer.getName());
        }

        currentPlayer.getInventory().setContents(
                playerInventories.get(currentPlayer.getName()));

        currentPlayer.getInventory().setArmorContents(
                playerArmour.get(currentPlayer.getName()));
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
            event.setDamage(0.0);

            damagedPlayer.addPotionEffects(playerEffects.get(damagedPlayer
                    .getName()));

            damagedPlayer.addPotionEffect(
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                            Functions.SecondsToTicks(3), 4), true);
            playerDeathProtection.remove(damagedPlayer.getName());
        }

    }

}
