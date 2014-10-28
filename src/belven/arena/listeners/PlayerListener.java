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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import resources.EntityFunctions;
import resources.Functions;
import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.BaseArena.ArenaTypes;
import belven.arena.arenas.PvPArena;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.challengeclasses.ChallengeType.ChallengeTypes;
import belven.arena.challengeclasses.PlayerSacrifice;

public class PlayerListener implements Listener {
	private final ArenaManager plugin;
	Random randomGenerator = new Random();

	public HashMap<String, Location> warpLocations = new HashMap<String, Location>();
	public ArrayList<String> playerDeathProtection = new ArrayList<String>();
	public HashMap<String, ItemStack[]> playerInventories = new HashMap<String, ItemStack[]>();
	public HashMap<String, ItemStack[]> playerArmour = new HashMap<String, ItemStack[]>();
	public HashMap<String, Collection<PotionEffect>> playerEffects = new HashMap<String, Collection<PotionEffect>>();

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
		if (plugin != null && plugin.IsPlayerInArena(event.getPlayer()) && plugin.getArena(event.getPlayer()).isActive) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		if (plugin.IsPlayerInArena(event.getPlayer())) {
			BaseArena ab = plugin.getArena(event.getPlayer());

			if (ab.type != ArenaTypes.Temp && event.getTo().getWorld() == ab.LocationToCheckForPlayers.getWorld()) {
				if (!event.getTo().getBlock().hasMetadata("ArenaAreaBlock")) {
					plugin.LeaveArena(event.getPlayer());
				}
			}
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
		Sign currentSign;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			List<MetadataValue> mData = MDM.getMetaData(MDM.ChallengeBlock, event.getClickedBlock());
			if (mData != null) {
				ChallengeBlock cb = (ChallengeBlock) mData.get(0).value();

				if (cb.challengeType.type == ChallengeTypes.PlayerSacrifice) {
					PlayerSacrifice ps = (PlayerSacrifice) cb.challengeType;
					ps.SacrificePlayer(event.getPlayer());
				} else if (cb.challengeType.type == ChallengeTypes.ItemSacrifice) {
					// TO DO
				}
			} else if (event.getClickedBlock().getType() == Material.SIGN) {
				currentSign = (Sign) event.getClickedBlock();

				if (currentSign.getLine(0) != null && currentSign.getLine(0).contentEquals("[Arena]")) {
					plugin.WarpToArena(event.getPlayer(), currentSign.getLine(1));
				} else if (currentSign.getLine(0) != null && currentSign.getLine(0).contentEquals("[ArenaLeave]")) {
					plugin.LeaveArena(event.getPlayer());
				}
			} else if (event.getClickedBlock().getType() == Material.WALL_SIGN) {
				currentSign = (Sign) event.getClickedBlock().getState();

				if (currentSign.getLine(0) != null && currentSign.getLine(0).contentEquals("[Arena]")) {
					plugin.WarpToArena(event.getPlayer(), currentSign.getLine(1));
				} else if (currentSign.getLine(0) != null && currentSign.getLine(0).contentEquals("[ArenaLeave]")) {
					plugin.LeaveArena(event.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player p = event.getEntity();
		event.getDrops().clear();

		event.setNewLevel(p.getLevel());

		if (plugin.IsPlayerInArena(p)) {
			BaseArena ab = plugin.getArena(p);

			if (ab.type == ArenaTypes.PvP) {
				PvPArena pvpa = (PvPArena) ab;
				pvpa.PlayerKilled(p);
			}

			Location spawnLocation = BaseArena.GetRandomArenaSpawnLocation(ab);
			warpLocations.put(p.getName(), spawnLocation);
			playerEffects.put(p.getName(), p.getActivePotionEffects());
		}

		PlayerInventory pi = p.getInventory();
		playerInventories.put(p.getName(), pi.getContents());
		playerArmour.put(p.getName(), p.getInventory().getArmorContents());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		Player currentPlayer = event.getPlayer();

		if (warpLocations.get(currentPlayer.getName()) != null) {
			Location spawnLocation = warpLocations.get(currentPlayer.getName());

			if (spawnLocation != null) {
				spawnLocation = Functions.offsetLocation(spawnLocation, 0.5, 0, 0.5);

				event.setRespawnLocation(spawnLocation);
			}

			warpLocations.put(currentPlayer.getName(), null);
			playerDeathProtection.add(currentPlayer.getName());
		}

		if (playerInventories.containsKey(currentPlayer.getName())) {
			currentPlayer.getInventory().setContents(playerInventories.get(currentPlayer.getName()));
		}

		if (playerArmour.containsKey(currentPlayer.getName())) {
			currentPlayer.getInventory().setArmorContents(playerArmour.get(currentPlayer.getName()));
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			PlayerTakenDamage(event);
		}
	}

	public void PlayerTakenDamage(EntityDamageByEntityEvent event) {
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
