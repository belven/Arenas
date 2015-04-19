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
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;

import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.BaseArenaData.ArenaTypes;
import belven.arena.arenas.StandardArena;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.phases.KillsPhase;
import belven.arena.rewardclasses.Reward;

public class MobListener implements Listener {
	public HashMap<String, String> CurrentPlayerClasses = new HashMap<String, String>();

	Random randomGenerator = new Random();

	public MobListener(ArenaManager instance) {
	}

	@EventHandler
	public void onEntityCombustEvent(EntityCombustEvent event) {
		if (event.getEntity().hasMetadata("ArenaMob") && event.getDuration() == 8) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity().hasMetadata("ArenaMob") && event.getCause() == DamageCause.SUFFOCATION) {
			List<MetadataValue> data = MDM.getMetaData(MDM.ArenaMob, event.getEntity());
			BaseArena ab = (BaseArena) data.get(0).value();
			event.getEntity().teleport(BaseArena.GetRandomArenaSpawnLocation(ab));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		event.blockList().clear();
	}

	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent event) {
		if (event.getEntity() == null) {
			return;
		}

		if (event.getEntityType() == EntityType.ARROW) {
			Arrow a = (Arrow) event.getEntity();
			if (a.getShooter() != null && ((LivingEntity) a.getShooter()).getType() == EntityType.SKELETON) {
				LivingEntity le = (LivingEntity) a.getShooter();
				if (le.hasMetadata("ArenaMob")) {
					a.remove();
				}
			}
		}
	}

	// @EventHandler
	// public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
	// if (event.getTarget() != null && event.getTarget().getType() == EntityType.PLAYER) {
	// Player p = (Player) event.getTarget();
	// Entity entity = event.getEntity();
	//
	// if (entity != null && entity.hasMetadata("ArenaMob") && !plugin.IsPlayerInArena(p)) {
	// event.setCancelled(true);
	// } else if (entity != null && !entity.hasMetadata("ArenaMob") && plugin.IsPlayerInArena(p)) {
	// event.setCancelled(true);
	// } else if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
	// event.setCancelled(true);
	// }
	// }
	// }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeathEvent(EntityDeathEvent event) {
		Entity e = event.getEntity();
		List<MetadataValue> data = MDM.getMetaData(MDM.ArenaMob, e);
		List<MetadataValue> reward = MDM.getMetaData(MDM.RewardBoss, e);
		List<MetadataValue> killPhase = MDM.getMetaData(KillsPhase.KILLS_PHASE, e);

		if (killPhase != null) {
			KillsPhase kp = (KillsPhase) killPhase.get(0).value();
			kp.EntityKilled(e);
		}

		if (data != null) {
			BaseArena ab = (BaseArena) data.get(0).value();

			if (ab == null) {
				return;
			}

			if (reward != null) {
				Reward r = (Reward) reward.get(0);
				r.GiveRewards(ab.getCurrentChallengeBlock(), ab.getArenaPlayers());
			}

			if (ab.getType() != ArenaTypes.PvP) {
				StandardArena sab = (StandardArena) ab;

				sab.getArenaEntities().remove(e);

				if (sab.getArenaEntities().size() <= 0 && sab.getCurrentRunTimes() <= sab.getMaxRunTimes()) {
					sab.ProgressingWave();
				}
			}

			for (Player p : ab.getArenaPlayers()) {
				p.giveExp(event.getDroppedExp());
			}

			if (ab.getCurrentChallengeBlock() != null) {
				ChallengeBlock cb = ab.getCurrentChallengeBlock();
				cb.challengeType.EntityKilled(e.getType());
			}

			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}
}
