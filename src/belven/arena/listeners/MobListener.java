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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;
import belven.arena.blocks.ChallengeBlock;
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
		if (event.getDuration() == 8) {
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityCombustEvent(ProjectileHitEvent event) {
		if (event.getEntityType() == EntityType.ARROW) {
			Arrow a = (Arrow) event.getEntity();
			if (a.getShooter() != null
					&& a.getShooter().getType() == EntityType.SKELETON) {
				LivingEntity le = a.getShooter();
				if (le.hasMetadata("ArenaMob")) {
					a.remove();
				}
			}
		}
	}

	@EventHandler
	public void onEntityTargetLivingEntityEvent(
			EntityTargetLivingEntityEvent event) {
		if (event.getTarget() != null
				&& event.getTarget().getType() == EntityType.PLAYER) {

			Player p = (Player) event.getTarget();

			if (event.getEntity() != null
					&& event.getEntity().hasMetadata("ArenaMob")
					&& !plugin.IsPlayerInArena(p)) {
				event.setCancelled(true);
			} else if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		Entity e = event.getEntity();

		if (e.hasMetadata("ArenaMob")) {
			List<MetadataValue> currentMetaData = e.getMetadata("ArenaMob");

			if (currentMetaData.size() == 0) {
				return;
			}

			String arena = currentMetaData.get(0).asString();

			if (arena != null) {
				ArenaBlock ab = plugin.getArenaBlock(arena);

				if (ab == null)
					return;

				ab.ArenaEntities.remove(e);

				if (ab.ArenaEntities.size() <= 0
						&& ab.currentRunTimes <= ab.maxRunTimes) {
					ab.GoToNextWave();
				}

				for (Player p : ab.arenaPlayers) {
					p.giveExp(event.getDroppedExp());
				}

				if (ab.currentChallengeBlock != null) {
					ChallengeBlock cb = ab.currentChallengeBlock;

					if (!cb.completed
							&& cb.challengeType.type == ChallengeTypes.Kills) {
						Kills ct = (Kills) cb.challengeType;
						ct.EntityKilled(e.getType());
						cb.SetPlayersScoreboard();
					}
				}
			}
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}
}
