package belven.arena.blocks;

import java.util.List;

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

import resources.Gear;
import belven.arena.ArenaManager;
import belven.arena.challengeclasses.ChallengeType;
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

    public ArenaManager plugin;
    public List<Player> players;
    public ArenaBlock ab;

    public ChallengeBlock(ArenaManager instance, Block b, Reward r,
            ChallengeType ct)
    {
        challengeReward = r;
        challengeType = ct;
        challengeBlock = b;
        plugin = instance;
        plugin.challengeBlocks.add(this);
        challengeBlockState = b.getState();
        b.setType(Material.DIAMOND_BLOCK);
    }

    public ChallengeBlock(ArenaManager instance, Block b, Reward r,
            ChallengeType ct, List<Player> playersToAdd)
    {
        this(instance, b, r, ct);
        players = playersToAdd;
    }

    public ChallengeBlock(ArenaManager instance, Block b, Reward r,
            ChallengeType ct, ArenaBlock arenaBlock)
    {
        this(instance, b, r, ct);
        ab = arenaBlock;
        new MessageTimer(ab.arenaPlayers, "A challenge of type "
                + ct.challengeType.name() + " has begun").run();
    }

    public void GiveRewards()
    {
        if (ab != null)
        {
            players = ab.arenaPlayers;

            new MessageTimer(ab.arenaPlayers,
                    "Challenge has been completed you get "
                            + challengeReward.rewardType.name()).run();
        }

        if (challengeReward.rewardType == RewardType.Experience)
        {
            int exp = ((ExperienceReward) challengeReward).experience;
            for (Player p : players)
            {
                p.giveExp(exp);
            }
        }
        else if (challengeReward.rewardType == RewardType.Items)
        {
            List<ItemStack> items = ((ItemReward) challengeReward).rewards;
            for (Player p : players)
            {
                for (ItemStack is : items)
                {
                    p.getInventory().addItem(is);
                }
            }
        }
        else if (challengeReward.rewardType == RewardType.Boss)
        {
            EntityType et = ((BossReward) challengeReward).boss;

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

        plugin.challengeBlocks.remove(this);
        challengeBlockState.update(true);
        completed = true;
    }

    public static ChallengeBlock RandomChallengeBlock(ArenaManager instance,
            ArenaBlock ab)
    {
        ChallengeBlock cb = null;
        Block b = ArenaBlock.GetRandomArenaSpawnBlock(ab).getRelative(
                BlockFace.UP);
        Reward r = Reward.GetRandomReward();
        ChallengeType ct = ChallengeType.GetRandomChallengeType(ab);
        cb = new ChallengeBlock(instance, b, r, ct, ab);
        return cb;
    }

}
