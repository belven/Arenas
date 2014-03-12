package belven.arena.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;

public class MobListener implements Listener
{
    private final ArenaManager plugin;
    private List<ItemStack> archerDrops = new ArrayList<ItemStack>();
    private List<ItemStack> mageDrops = new ArrayList<ItemStack>();
    private List<ItemStack> healerDrops = new ArrayList<ItemStack>();
    private List<ItemStack> assassinDrops = new ArrayList<ItemStack>();
    private List<ItemStack> warriorDrops = new ArrayList<ItemStack>();
    public HashMap<String, String> CurrentPlayerClasses = new HashMap<String, String>();

    Random randomGenerator = new Random();

    public MobListener(ArenaManager instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event)
    {
        Entity currentEntity = event.getEntity();
        //List<ItemStack> drops = event.getDrops();

        int currentRand = randomGenerator.nextInt(2);

        if (currentEntity.hasMetadata("ArenaMob"))
        {
            // event.getDrops().clear();
            // drops.clear();

            if (currentRand != 0)
            {
                return;
            }

            List<MetadataValue> currentMetaData = currentEntity
                    .getMetadata("ArenaMob");

            if (currentMetaData.size() == 0)
            {
                return;
            }

            List<String> playerList = Arrays.asList(currentMetaData.get(0)
                    .asString().split(" "));

            if (playerList.get(0) != null)
            {
                ArenaBlock ab = plugin.getArenaBlock(playerList.get(0));
                ab.ArenaEntities.remove(currentEntity);
            }
        }
    }

    @SuppressWarnings("unused")
    private String GetPlayerClass(String playerName)
    {
        plugin.reloadConfig();
        if (plugin.getConfig().contains(playerName + ".Class"))
        {
            return plugin.getConfig().getString(playerName + ".Class");
        }
        else
        {
            return "";
        }
    }

    @SuppressWarnings("unused")
    private void SetDrops()
    {
        archerDrops.clear();
        mageDrops.clear();
        healerDrops.clear();
        assassinDrops.clear();
        warriorDrops.clear();

        SetArcherDrops();
        SetMageDrops();
        SetHealerDrops();
        SetAssassinDrops();
        SetWarriorDrops();
    }

    private void SetWarriorDrops()
    {
        int currentRand = randomGenerator.nextInt(99);

        warriorDrops.add(new ItemStack(Material.BREAD, 3));

        if (currentRand > 30 && currentRand < 40)
        {
            warriorDrops.add(new ItemStack(Material.STONE_SWORD, 1));
        }
        else if (currentRand > 40 && currentRand < 50)
        {
            warriorDrops.add(new ItemStack(new Potion(PotionType.STRENGTH, 2)
                    .toItemStack(1)));
        }
        else if (currentRand > 50 && currentRand < 60)
        {
            warriorDrops.add(new ItemStack(Material.IRON_BOOTS, 1));
        }
        else if (currentRand > 60 && currentRand < 70)
        {
            warriorDrops.add(new ItemStack(Material.IRON_HELMET, 1));
        }
        else if (currentRand > 70 && currentRand < 80)
        {
            warriorDrops.add(new ItemStack(Material.IRON_LEGGINGS, 1));
        }
        else if (currentRand > 90 && currentRand < 100)
        {
            warriorDrops.add(new ItemStack(Material.IRON_CHESTPLATE, 1));
        }
    }

    @SuppressWarnings("deprecation")
    private void SetAssassinDrops()
    {
        int currentRand = randomGenerator.nextInt(99);
        assassinDrops.add(new ItemStack(Material.ARROW, 3));

        if (currentRand > 20 && currentRand < 30)
        {
            assassinDrops.add(new ItemStack(Material.BOW, 2));
        }
        else if (currentRand > 50 && currentRand < 60)
        {
            assassinDrops.add(new ItemStack(new Potion(PotionType.SPEED, 2)
                    .toItemStack(1)));
        }
        else if (currentRand > 60 && currentRand < 70)
        {
            assassinDrops.add(new ItemStack(new Potion(PotionType.NIGHT_VISION,
                    2, true).toItemStack(1)));
        }
        else if (currentRand > 70 && currentRand < 80)
        {
            // assassinDrops.add(new ItemStack(new Potion(
            // PotionType.WATER_BREATHING, 2).toItemStack(1)));
        }
        else if (currentRand > 90 && currentRand < 100)
        {
            assassinDrops.add(new ItemStack(Material.IRON_SWORD, 2));
        }
    }

    private void SetHealerDrops()
    {
        int currentRand = randomGenerator.nextInt(99);
        healerDrops.add(new ItemStack(Material.LAPIS_BLOCK, 3));

        if (currentRand > 30 && currentRand < 40)
        {
            healerDrops.add(new ItemStack(Material.PAPER, 1));
        }
        else if (currentRand > 40 && currentRand < 50)
        {
            healerDrops.add(new ItemStack(new Potion(PotionType.REGEN, 2)
                    .toItemStack(1)));
        }
        else if (currentRand > 50 && currentRand < 60)
        {
            healerDrops.add(new ItemStack(Material.LEATHER_BOOTS, 1));
        }
        else if (currentRand > 60 && currentRand < 70)
        {
            healerDrops.add(new ItemStack(Material.LEATHER_HELMET, 1));
        }
        else if (currentRand > 70 && currentRand < 80)
        {
            healerDrops.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
        }
        else if (currentRand > 90 && currentRand < 100)
        {
            healerDrops.add(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
        }
    }

    private void SetMageDrops()
    {
        int currentRand = randomGenerator.nextInt(99);

        mageDrops.add(new ItemStack(Material.LAPIS_BLOCK, 3));

        if (currentRand > 30 && currentRand < 40)
        {
            mageDrops.add(new ItemStack(Material.FEATHER, 2));
        }
        else if (currentRand > 40 && currentRand < 50)
        {
            mageDrops.add(new ItemStack(new Potion(PotionType.FIRE_RESISTANCE,
                    2).toItemStack(1)));
        }
        else if (currentRand > 50 && currentRand < 60)
        {
            mageDrops.add(new ItemStack(Material.LEATHER_BOOTS, 1));
        }
        else if (currentRand > 60 && currentRand < 70)
        {
            // mageDrops.add(new ItemStack(Material.LEATHER_HELMET, 1));
        }
        else if (currentRand > 70 && currentRand < 80)
        {
            mageDrops.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
        }
        else if (currentRand > 90 && currentRand < 100)
        {
            // mageDrops.add(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
        }
    }

    private void SetArcherDrops()
    {
        int currentRand = randomGenerator.nextInt(99);

        archerDrops.add(new ItemStack(Material.ARROW, 3));

        if (currentRand > 20 && currentRand < 30)
        {
            archerDrops.add(new ItemStack(Material.BOW, 2));
        }
        else if (currentRand > 30 && currentRand < 40)
        {
            archerDrops.add(new ItemStack(Material.WOOL, 2));
        }
        else if (currentRand > 50 && currentRand < 60)
        {
            archerDrops.add(new ItemStack(Material.LEATHER_BOOTS, 1));
        }
        else if (currentRand > 60 && currentRand < 70)
        {
            // archerDrops.add(new ItemStack(Material.LEATHER_HELMET, 1));
        }
        else if (currentRand > 70 && currentRand < 80)
        {
            // archerDrops.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
        }
        else if (currentRand > 90 && currentRand < 100)
        {
            archerDrops.add(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
        }
    }

}
