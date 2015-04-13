package belven.arena.challengeclasses;

import java.util.Random;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.Scoreboard;

import belven.arena.arenas.StandardArena;
import belven.arena.phases.Interactable;

public abstract class Challenge extends Interactable {
	public enum ChallengeTypes {
		Default, Kills, PlayerSacrifice, ItemSacrifice
	}

	public UUID challengeID = UUID.randomUUID();
	public ChallengeTypes type = ChallengeTypes.Default;
	private ChallengeBlock cb;

	public Challenge(ChallengeBlock cb) {
		this.setChallengeBlock(cb);
	}

	public abstract boolean ChallengeComplete();

	public abstract void EntityKilled(EntityType entityType);

	public abstract Scoreboard SetChallengeScoreboard();

	public static Challenge GetRandomChallengeType(ChallengeBlock cb, StandardArena ab) {
		int ran = new Random().nextInt(2);
		switch (ran) {
		case 0:
			return new Kills(cb, Kills.GetRandomEntities(ab));
		case 1:
			return new PlayerSacrifice(cb, ab.getArenaPlayers());
		default:
			return new Kills(cb, Kills.GetRandomEntities(ab));
		}
	}

	public ChallengeBlock getChallengeBlock() {
		return cb;
	}

	public void setChallengeBlock(ChallengeBlock cb) {
		this.cb = cb;
	}
}