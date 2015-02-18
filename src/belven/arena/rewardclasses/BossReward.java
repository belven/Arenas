package belven.arena.rewardclasses;

import java.util.List;

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

		Gear bossGear = ArenaManager.scalingGear.get(players.size());
		bossGear.SetGear(le);

		le.setMetadata(MDM.RewardBoss, new FixedMetadataValue(cb.plugin, new ExperienceReward(30)));

		new MessageTimer(players, messtext + rewardType.name()).run();
	}
}
