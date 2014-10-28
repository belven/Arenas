package belven.arena.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.BaseArena.ArenaTypes;
import belven.arena.arenas.StandardArena;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.challengeclasses.ChallengeType.ChallengeTypes;
import belven.arena.challengeclasses.Kills;

public class MobListener implements Listener {
	private final ArenaManager plugin;
	public HashMap<String, String> CurrentPlayerClasses = new HashMap<String, String>();

	Random randomGenerator = new Random();

	public MobListener(ArenaManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onEntityCombustEvent(EntityCombustEvent event) {
		if (event.getEntity().hasMetadata("ArenaMob") && event.getDuration() == 8) {
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent event) {
		if (event.getEntity() == null) {
			return;
		}

		if (event.getEntityType() == EntityType.ARROW) {
			Arrow a = (Arrow) event.getEntity();
			if (a.getShooter() != null && a.getShooter().getType() == EntityType.SKELETON) {
				LivingEntity le = a.getShooter();
				if (le.hasMetadata("ArenaMob")) {
					a.remove();
				}
			}
		}
	}

	@EventHandler
	public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() != null && event.getTarget().getType() == EntityType.PLAYER) {

			Player p = (Player) event.getTarget();

			if (event.getEntity() != null && event.getEntity().hasMetadata("ArenaMob") && !plugin.IsPlayerInArena(p)) {
				event.setCancelled(true);
			} else if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
		if (event.getEntity() != null && event.getEntity().hasMetadata(MDM.ArenaMob)) {
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		Entity e = event.getEntity();
		List<MetadataValue> data = MDM.getMetaData(MDM.ArenaMob, e);

		if (data != null) {
			BaseArena ab = (BaseArena) data.get(0).value();

			if (ab == null) {
				return;
			}

			if (ab.type != ArenaTypes.PvP) {
				StandardArena sab = (StandardArena) ab;

				sab.ArenaEntities.remove(e);

				if (sab.ArenaEntities.size() <= 0 && sab.currentRunTimes <= sab.maxRunTimes) {
					sab.GoToNextWave();
				}
			}

			for (Player p : ab.arenaPlayers) {
				p.giveExp(event.getDroppedExp());
			}

			if (ab.currentChallengeBlock != null) {
				ChallengeBlock cb = ab.currentChallengeBlock;

				if (!cb.completed && cb.challengeType.type == ChallengeTypes.Kills) {
					Kills ct = (Kills) cb.challengeType;
					ct.EntityKilled(e.getType());
					cb.SetPlayersScoreboard();
				}
			}
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}
}
