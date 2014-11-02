package belven.arena.challengeclasses;

import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import belven.arena.ArenaManager;
import belven.arena.MDM;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.StandardArena;
import belven.arena.rewardclasses.ExperienceReward;
import belven.arena.rewardclasses.Reward;
import belven.arena.timedevents.MessageTimer;

public class ChallengeBlock {
	public static ChallengeBlock RandomChallengeBlock(ArenaManager instance, StandardArena ab) {
		ChallengeBlock cb = null;
		Block b = BaseArena.GetRandomArenaSpawnBlock(ab);
		Reward r = Reward.GetRandomReward();
		ChallengeType ct = ChallengeType.GetRandomChallengeType(ab);
		cb = new ChallengeBlock(instance, b, r, ct, ab);
		return cb;
	}

	public boolean completed = false;
	public Reward challengeReward = new ExperienceReward(10);
	public ChallengeType challengeType = null;
	public Block challengeBlock;
	public BlockState challengeBlockState;

	public UUID ID;
	public ArenaManager plugin;
	public List<Player> players;

	public BaseArena ab;

	public ChallengeBlock(ArenaManager instance, Block b, Reward r, ChallengeType ct) {
		ID = UUID.randomUUID();
		challengeReward = r;
		challengeType = ct;
		challengeBlock = b;
		plugin = instance;
		plugin.challengeBlocks.add(this);
		challengeBlockState = b.getState();
		b.setType(Material.DIAMOND_BLOCK);
		b.setMetadata(MDM.ChallengeBlock, new FixedMetadataValue(plugin, this));
	}

	public ChallengeBlock(ArenaManager instance, Block b, Reward r, ChallengeType ct, BaseArena arenaBlock) {
		this(instance, b, r, ct);
		ab = arenaBlock;
		new MessageTimer(ab.getArenaPlayers(), "A challenge of type " + ct.type.name() + " has begun").run();
		SetPlayersScoreboard();
	}

	public ChallengeBlock(ArenaManager instance, Block b, Reward r, ChallengeType ct, List<Player> playersToAdd) {
		this(instance, b, r, ct);
		players = playersToAdd;
		SetPlayersScoreboard();
	}

	public void GiveRewards() {
		challengeReward.GiveRewards(this, ab != null ? ab.getArenaPlayers() : players);
		plugin.challengeBlocks.remove(this);
		challengeBlockState.update(true);
		completed = true;
	}

	public void SetPlayersScoreboard() {
		List<Player> tempPlayers = ab != null ? ab.getArenaPlayers() : players;

		for (Player p : tempPlayers) {
			p.setScoreboard(challengeType.SetChallengeScoreboard(challengeType));
		}
	}
}
