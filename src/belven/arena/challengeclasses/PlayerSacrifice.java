package belven.arena.challengeclasses;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import belven.arena.events.ChallengeComplete;

public class PlayerSacrifice extends ChallengeType
{
    public List<Player> playersSacrificed = new ArrayList<Player>();
    public int amountToSacrifice = 1;

    public PlayerSacrifice()
    {
        challengeType = ChallengeTypes.PlayerSacrifice;
    }

    public void SacricePlayer(Player p)
    {
        playersSacrificed.add(p);
        amountToSacrifice--;

        if (ChallengeComplete())
        {
            Bukkit.getPluginManager().callEvent(new ChallengeComplete(this));
        }
    }

    @Override
    public boolean ChallengeComplete()
    {
        return amountToSacrifice <= 0;
    }
}
