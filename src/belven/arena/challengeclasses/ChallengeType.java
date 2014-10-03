package belven.arena.challengeclasses;

import java.util.Random;
import java.util.UUID;

import org.bukkit.scoreboard.Scoreboard;

import belven.arena.arenas.StandardArena;

public abstract class ChallengeType {

	public enum ChallengeTypes {
		Default, Kills, PlayerSacrifice, ItemSacrifice
	}

	public UUID challengeID = UUID.randomUUID();
	public ChallengeTypes type = ChallengeTypes.Default;

	public abstract boolean ChallengeComplete();

	public abstract Scoreboard SetChallengeScoreboard(ChallengeType ct);

	public static ChallengeType GetRandomChallengeType(StandardArena ab) {
		int ran = new Random().nextInt(2);
		switch (ran) {
		case 0:
			return new Kills(Kills.GetRandomEntities(ab));
		case 1:
			return new PlayerSacrifice(ab.arenaPlayers.size());
		default:
			return new Kills(Kills.GetRandomEntities(ab));
		}
	}
}