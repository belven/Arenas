package belven.arena.rewardclasses;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.timedevents.MessageTimer;
import belven.resources.Gear;
import belven.resources.events.EntityMetadataChanged;

public class BossReward extends Reward {
	public EntityType boss;

	public BossReward(EntityType et) {
		rewardType = RewardType.Boss;
		boss = et;
	}

	@Override
	public void GiveRewards(ChallengeBlock cb, List<Player> players) {

		String messtext = "Challenge has been completed you get ";

		EntityType et = boss;

		messtext += " a " + et.name();

		Location SpawnLocation = cb.challengeBlock.getRelative(BlockFace.UP).getLocation();

		LivingEntity le = (LivingEntity) cb.challengeBlock.getWorld().spawnEntity(SpawnLocation, et);
		// FixedMetadataValue metaData = new FixedMetadataValue(getPlugin(), ab);

		FixedMetadataValue metaData = new FixedMetadataValue(cb.plugin, "");
		le.setMetadata(MDM.ArenaBoss, metaData);
		le.setMetadata(MDM.RewardBoss, new FixedMetadataValue(cb.plugin, new ExperienceReward(Reward.getExpRewards()
				.get(Reward.getExpRewards().size() - 1))));

		Bukkit.getPluginManager().callEvent(new EntityMetadataChanged(metaData, le));

		Gear bossGear = ArenaManager.scalingGear.get(players.size());
		bossGear.SetGear(le);

		new MessageTimer(players, messtext + rewardType.name()).run();
	}
}
