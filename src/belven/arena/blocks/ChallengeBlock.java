package belven.arena.blocks;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import resources.Gear;
import belven.arena.ArenaManager;
import belven.arena.challengeclasses.ChallengeType;
import belven.arena.challengeclasses.ChallengeType.ChallengeTypes;
import belven.arena.challengeclasses.Kills;
import belven.arena.challengeclasses.PlayerSacrifice;
import belven.arena.rewardclasses.BossReward;
import belven.arena.rewardclasses.ExperienceReward;
import belven.arena.rewardclasses.ItemReward;
import belven.arena.rewardclasses.Reward;
import belven.arena.rewardclasses.Reward.RewardType;
import belven.arena.timedevents.MessageTimer;

public class ChallengeBlock
{
    public boolean completed = false;
    public Reward challengeReward = new ExperienceReward(10);
    public ChallengeType challengeType = null;
    public Block challengeBlock;
    public BlockState challengeBlockState;
    public UUID ID;

    public ArenaManager plugin;
    public List<Player> players;
    public ArenaBlock ab;

    public ChallengeBlock(ArenaManager instance, Block b, Reward r,
            ChallengeType ct)
    {
        ID = UUID.randomUUID();
        challengeReward = r;
        challengeType = ct;
        challengeBlock = b;
        plugin = instance;
        plugin.challengeBlocks.add(this);
        challengeBlockState = b.getState();
        b.setType(Material.DIAMOND_BLOCK);
        b.setMetadata("Challenge Block", new FixedMetadataValue(plugin, this));
    }

    public void SetPlayersScoreboard()
    {
        List<Player> tempPlayers = ab != null ? ab.arenaPlayers : players;

        for (Player p : tempPlayers)
        {
            p.setScoreboard(SetChallengeScoreboard(challengeType));
        }

    }

    public static Scoreboard SetChallengeScoreboard(ChallengeType ct)
    {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard sb = manager.getNewScoreboard();

        if (ct.type == ChallengeTypes.Kills)
        {
            Kills kills = (Kills) ct;

            if (!kills.ChallengeComplete())
            {
                Objective objective = sb.registerNewObjective("test", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                objective.setDisplayName("Kill Challenge");

                for (EntityType et : kills.entitiesToKill.keySet())
                {
                    Score score = objective.getScore(et.name());
                    score.setScore(kills.entitiesToKill.get(et));
                }
            }
        }
        else if (ct.type == ChallengeTypes.PlayerSacrifice)
        {
            PlayerSacrifice ps = (PlayerSacrifice) ct;

            if (!ps.ChallengeComplete())
            {
                Objective objective = sb.registerNewObjective("test", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.setDisplayName("Sacrifice Challenge");
                Score score = objective.getScore("Amount Left: ");
                score.setScore(ps.amountToSacrifice);
            }
        }
        return sb;
    }

    public ChallengeBlock(ArenaManager instance, Block b, Reward r,
            ChallengeType ct, List<Player> playersToAdd)
    {
        this(instance, b, r, ct);
        players = playersToAdd;
        SetPlayersScoreboard();
    }

    public ChallengeBlock(ArenaManager instance, Block b, Reward r,
            ChallengeType ct, ArenaBlock arenaBlock)
    {
        this(instance, b, r, ct);
        ab = arenaBlock;
        new MessageTimer(ab.arenaPlayers, "A challenge of type "
                + ct.type.name() + " has begun").run();
        SetPlayersScoreboard();
    }

    public void GiveRewards()
    {
        if (ab != null)
        {
            players = ab.arenaPlayers;
        }

        String messtext = "Challenge has been completed you get ";

        if (challengeReward.rewardType != RewardType.Boss)
        {
            for (Player p : players)
            {
                p.setScoreboard(Bukkit.getScoreboardManager()
                        .getNewScoreboard());

                if (challengeReward.rewardType == RewardType.Experience)
                {
                    double exp = ((ExperienceReward) challengeReward).experience;

                    int expToGive = (int) (p.getExpToLevel() * exp);
                    p.giveExp(expToGive);

                    messtext += String.valueOf(expToGive) + " ";
                }
                else if (challengeReward.rewardType == RewardType.Items)
                {
                    List<ItemStack> items = ((ItemReward) challengeReward).rewards;

                    for (ItemStack is : items)
                    {
                        messtext += is.getType().name() + " "
                                + String.valueOf(is.getAmount() + " ");

                        p.getInventory().addItem(is);
                    }
                }
            }
        }
        else if (challengeReward.rewardType == RewardType.Boss)
        {
            EntityType et = ((BossReward) challengeReward).boss;

            messtext += " a " + et.name();

            Location SpawnLocation = challengeBlock.getRelative(BlockFace.UP)
                    .getLocation();

            LivingEntity le = (LivingEntity) challengeBlock.getWorld()
                    .spawnEntity(SpawnLocation, et);

            Gear bossGear = ArenaManager.scalingGear.get(players.size());
            le.getEquipment().setHelmet(bossGear.h);
            le.getEquipment().setChestplate(bossGear.c);
            le.getEquipment().setLeggings(bossGear.l);
            le.getEquipment().setBoots(bossGear.b);
            le.getEquipment().setItemInHand(bossGear.w);

            le.setMetadata("RewardBoss", new FixedMetadataValue(plugin,
                    new ExperienceReward(30)));
        }

        new MessageTimer(ab.arenaPlayers, messtext
                + challengeReward.rewardType.name()).run();

        plugin.challengeBlocks.remove(this);
        challengeBlockState.update(true);
        completed = true;
    }

    public static ChallengeBlock RandomChallengeBlock(ArenaManager instance,
            ArenaBlock ab)
    {
        ChallengeBlock cb = null;
        Block b = ArenaBlock.GetRandomArenaSpawnBlock(ab);
        Reward r = Reward.GetRandomReward();
        ChallengeType ct = ChallengeType.GetRandomChallengeType(ab);
        cb = new ChallengeBlock(instance, b, r, ct, ab);
        return cb;
    }

}
