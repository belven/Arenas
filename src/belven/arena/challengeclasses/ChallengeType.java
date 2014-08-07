package belven.arena.challengeclasses;

public abstract class ChallengeType
{
    enum Type
    {
        Default, Kills, PlayerSacrifice, ItemSacrifice
    }

    Type challengeType = Type.Default;

}
