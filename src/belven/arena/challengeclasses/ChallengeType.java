package belven.arena.challengeclasses;

import java.util.Random;
import java.util.UUID;

import belven.arena.blocks.ArenaBlock;

public abstract class ChallengeType
{

    public enum ChallengeTypes
    {
        Default, Kills, PlayerSacrifice, ItemSacrifice
    }

    public UUID challengeID = UUID.randomUUID();
    public ChallengeTypes type = ChallengeTypes.Default;

    public abstract boolean ChallengeComplete();

    public static ChallengeType GetRandomChallengeType(ArenaBlock ab)
    {
        int ran = new Random().nextInt(2);

        switch (ran)
        {
        case 0:
            return new Kills(Kills.GetRandomEntities(ab));
        case 1:
            return new PlayerSacrifice(ab.arenaPlayers.size());
        default:
            return new Kills(Kills.GetRandomEntities(ab));
        }

    }

}
