package belven.arena.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.BaseArena.ArenaTypes;
import belven.arena.arenas.PvPArena;
import belven.arena.phases.Interactable;
import belven.resources.EntityFunctions;
import belven.resources.Functions;

public class PlayerListener implements Listener {
	private final ArenaManager plugin;
	Random randomGenerator = new Random();

	public HashMap<String, Location> warpLocations = new HashMap<>();
	public ArrayList<String> playerDeathProtection = new ArrayList<>();
	public HashMap<String, ItemStack[]> playerInventories = new HashMap<>();
	public HashMap<String, ItemStack[]> playerArmour = new HashMap<>();
	public HashMap<String, Collection<PotionEffect>> playerEffects = new HashMap<>();

	public PlayerListener(ArenaManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Entity e = event.getEntity();
		LivingEntity le = EntityFunctions.GetDamager(event);

		if (le != null && le.getType() == EntityType.PLAYER) {
			Player p = (Player) le;

			List<MetadataValue> currentMetaData = MDM.getMetaData(MDM.ArenaMob, e);

			if (!plugin.IsPlayerInArena(p)) {
				// Is entity a mob?
				if (currentMetaData != null) {
					BaseArena currentArena = (BaseArena) currentMetaData.get(0).value();

					plugin.WarpToArena(p, currentArena);
					// Is it a player?
				} else if (e.getType() == EntityType.PLAYER) {
					Player pOther = (Player) e;

					// Is the other player in an arena?
					if (plugin.IsPlayerInArena(pOther)) {
						BaseArena currentArena = plugin.getArena(pOther);
						plugin.WarpToArena(p, currentArena);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		if (plugin.IsPlayerInArena(event.getPlayer())) {
			plugin.LeaveArena(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent event) {
		plugin.onlinePlayers.add(event.getPlayer());
	}

	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (plugin != null && plugin.IsPlayerInArena(event.getPlayer())
				&& plugin.getArena(event.getPlayer()).isActive()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerVelocityEvent(PlayerVelocityEvent event) {
		if (event.getPlayer().isBlocking()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().hasMetadata(MDM.ChallengeBlock)) {
				Interactable interactable = (Interactable) getMetaData(event, MDM.ChallengeBlock);
				interactable.interactedWith(event.getPlayer());
			} else if (event.getClickedBlock().hasMetadata(Interactable.metadataName)) {
				Interactable interactable = (Interactable) getMetaData(event, Interactable.metadataName);
				interactable.interactedWith(event.getPlayer());
			} else if (event.getClickedBlock().getType() == Material.SIGN) {
				arenaSignInteraction(event, (Sign) event.getClickedBlock());
			} else if (event.getClickedBlock().getType() == Material.WALL_SIGN) {
				arenaSignInteraction(event, (Sign) event.getClickedBlock().getState());
			}
		}
	}

	private Object getMetaData(PlayerInteractEvent event, String metaDataName) {
		List<MetadataValue> mData = MDM.getMetaData(metaDataName, event.getClickedBlock());
		return mData.get(0).value();
	}

	private void arenaSignInteraction(PlayerInteractEvent event, Sign currentSign) {
		if (currentSign.getLine(0) != null && currentSign.getLine(0).contentEquals("[Arena]")) {
			plugin.WarpToArena(event.getPlayer(), currentSign.getLine(1));
		} else if (currentSign.getLine(0) != null && currentSign.getLine(0).contentEquals("[ArenaLeave]")) {
			plugin.LeaveArena(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player p = event.getEntity();

		if (!playerInventories.containsKey(p)) {
			PlayerInventory pi = p.getInventory();
			playerInventories.put(p.getName(), pi.getContents());
			playerArmour.put(p.getName(), p.getInventory().getArmorContents());
		}

		event.setNewLevel(p.getLevel());

		if (plugin.IsPlayerInArena(p)) {
			BaseArena ab = plugin.getArena(p);

			if (ab.getType() == ArenaTypes.PvP) {
				PvPArena pvpa = (PvPArena) ab;
				pvpa.PlayerKilled(p);
			}

			Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(ab);
			warpLocations.put(p.getName(), spawnLocation);
			playerEffects.put(p.getName(), p.getActivePotionEffects());

			plugin.writeToLog("Player " + p.getName() + " has died in arena" + ab.getName());
		} else {
			plugin.writeToLog("Player " + p.getName() + " has died");

		}

		event.getDrops().clear();

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		Player currentPlayer = event.getPlayer();

		if (warpLocations.get(currentPlayer.getName()) != null) {
			Location spawnLocation = warpLocations.get(currentPlayer.getName());

			if (spawnLocation != null) {
				event.setRespawnLocation(spawnLocation);
			}

			warpLocations.put(currentPlayer.getName(), null);
			playerDeathProtection.add(currentPlayer.getName());
		}

		if (playerInventories.containsKey(currentPlayer.getName())) {
			currentPlayer.getInventory().setContents(playerInventories.get(currentPlayer.getName()));
			playerInventories.remove(currentPlayer.getName());
		}

		if (playerArmour.containsKey(currentPlayer.getName())) {
			currentPlayer.getInventory().setArmorContents(playerArmour.get(currentPlayer.getName()));
			playerArmour.remove(currentPlayer.getName());
		}
		plugin.writeToLog("Player " + currentPlayer.getName() + " has respawned");
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player damagedPlayer = (Player) event.getEntity();

			if (playerDeathProtection.contains(damagedPlayer.getName())) {
				event.setDamage(0.0);
				damagedPlayer.addPotionEffects(playerEffects.get(damagedPlayer.getName()));
				damagedPlayer.addPotionEffect(
						new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Functions.SecondsToTicks(3), 4), true);
				playerDeathProtection.remove(damagedPlayer.getName());
			}
		}
	}
}