package belven.arena.rewardclasses;

public abstract class Reward
{
    enum Type
    {
        Default, Items, Experience
    }

    Type rewardType = Type.Default;
}
