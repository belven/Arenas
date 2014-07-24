package belven.arena.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;

import belven.arena.ArenaManager;
import belven.arena.blocks.ArenaBlock;

public class MobListener implements Listener
{
    private final ArenaManager plugin;
    public HashMap<String, String> CurrentPlayerClasses = new HashMap<String, String>();

    Random randomGenerator = new Random();

    public MobListener(ArenaManager instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onEntityCombustEvent(EntityCombustEvent event)
    {
        if (event.getDuration() == 8)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event)
    {
        Entity currentEntity = event.getEntity();
        // int currentRand = randomGenerator.nextInt(2);

        if (currentEntity.hasMetadata("ArenaMob"))
        {
            List<MetadataValue> currentMetaData = currentEntity
                    .getMetadata("ArenaMob");

            if (currentMetaData.size() == 0)
            {
                return;
            }

            List<String> arena = Arrays.asList(currentMetaData.get(0)
                    .asString().split(" "));

            if (arena.get(0) != null)
            {
                ArenaBlock ab = plugin.getArenaBlock(arena.get(0));
                ab.ArenaEntities.remove(currentEntity);

                if (ab.ArenaEntities.size() <= 0)
                {
                    ab.GoToNextWave();
                }

                for (Player p : ab.arenaPlayers)
                {
                    p.giveExp(event.getDroppedExp());
                }
            }
        }
        event.setDroppedExp(0);
    }

}
