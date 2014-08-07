package belven.arena.challengeclasses;

import java.util.Random;

import belven.arena.blocks.ArenaBlock;

public abstract class ChallengeType
{
    public ChallengeType()
    {

    }

    public enum ChallengeTypes
    {
        Default, Kills, PlayerSacrifice, ItemSacrifice
    }

    public ChallengeTypes challengeType = ChallengeTypes.Default;

    public abstract boolean ChallengeComplete();

    public static ChallengeType GetRandomChallengeType(ArenaBlock ab)
    {
        int ran = new Random().nextInt(2);

        switch (ran)
        {
        case 0:
            return new Kills(Kills.GetRandomEntities(ab));
        default:
            return new Kills(Kills.GetRandomEntities(ab));
        }

    }

}
