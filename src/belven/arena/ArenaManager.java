package belven.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import resources.EntityFunctions;
import resources.Functions;
import resources.Gear;
import belven.arena.blocks.ArenaBlock;
import belven.arena.blocks.StandardArenaBlock;
import belven.arena.blocks.TempArenaBlock;
import belven.arena.listeners.ArenaListener;
import belven.arena.listeners.BlockListener;
import belven.arena.listeners.MobListener;
import belven.arena.listeners.PlayerListener;
import belven.teams.TeamManager;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class ArenaManager extends JavaPlugin
{
    private final PlayerListener newplayerListener = new PlayerListener(this);
    private final BlockListener blockListener = new BlockListener(this);
    private final ArenaListener arenaListener = new ArenaListener(this);
    private final MobListener mobListener = new MobListener(this);

    public List<ArenaBlock> currentArenaBlocks = new ArrayList<ArenaBlock>();
    public HashMap<Player, ArenaBlock> SelectedArenaBlocks = new HashMap<Player, ArenaBlock>();

    private HashMap<Player, ArenaBlock> PlayersInArenas = new HashMap<Player, ArenaBlock>();
    public HashMap<String, Location> warpLocations = new HashMap<String, Location>();

    public TeamManager teams = (TeamManager) Bukkit.getServer()
            .getPluginManager().getPlugin("BelvensTeams");

    @Override
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(newplayerListener, this);
        pm.registerEvents(blockListener, this);
        pm.registerEvents(arenaListener, this);
        pm.registerEvents(mobListener, this);
        RecreateArenasFromConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args)
    {
        Player player = (Player) sender;
        String commandSent = cmd.getName();

        if (commandSent.equalsIgnoreCase("ba") && args.length > 0)
        {
            if (EditArenaCommand(player, args))
            {
                return true;
            }
            else if (ListArenaCommands(player, args))
            {
                return true;
            }
            else if (UtilityArenaCommands(player, args))
            {
                return true;
            }
        }
        return false;
    }

    private boolean UtilityArenaCommands(Player player, String[] args){
    	
    	switch(args[0]){
    	
    	case "cleararena":
    	case "ca":
    		ClearArena(player);
            return true;
        
    	case "forcestart":
    	case "fs":
    		ForceStartArena(player);
            return true;
            
    	case "reloadarena":
    	case "ra":
    		ReloadArena(player);
            return true;
            
    	case "givearenarewards":
    	case "gar":
    		GiveArenaRewards(player);
    		return true;
    		
    	case "portmobs":
    	case "pm":
    		TeleportArenaMobs(player);
            return true;
    	
    	case "warp":
    	case "w":
    		WarpToArena(player, args[1]);
            return true;
            
    	case "createtemparena":
    	case "cta":
    		CreateTempArena(player);
            return true;
    		
    	case "leave":
    	case "l":
    		LeaveArena(player);
    		return true;
    		
    	}
    	
    	return false;
    		
    }

    private void ReloadArena(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            ArenaBlock ab = GetSelectedArenaBlock(p);
            ReloadArenaFromConfig(ab.arenaName);
            p.sendMessage("Arena " + ab.ArenaName() + " has been reloaded");
        }
    }

    private void GiveArenaRewards(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            ArenaBlock ab = GetSelectedArenaBlock(p);
            ab.arenaPlayers.add(p);
            ab.GiveRewards();
            ab.arenaPlayers.remove(p);
            p.sendMessage("You were given arena " + ab.ArenaName()
                    + "s rewards");
        }
    }

    private void CreateTempArena(Player p)
    {
        if (!IsPlayerInArena(p))
        {
            int maxSize = 0;
            int Radius = 40;
            int period = 0;

            Location pLoc = p.getLocation();

            Player[] tempPlayers = EntityFunctions.getNearbyPlayersNew(pLoc,
                    (Radius - 2) + (Radius / 2));

            Radius = 0;

            for (Player pl : tempPlayers)
            {
                if (!IsPlayerInArena(pl))
                {
                    maxSize += 10;
                    Radius += 20;
                    period += 20;
                }
            }

            double MinX = pLoc.getX() - maxSize;
            double MinY = pLoc.getY() - 2;
            double MinZ = pLoc.getZ() - maxSize;

            double MaxX = pLoc.getX() + maxSize;
            double MaxY = pLoc.getY() + 4;
            double MaxZ = pLoc.getZ() + maxSize;

            Location min = new Location(p.getWorld(), MinX, MinY, MinZ);
            Location max = new Location(p.getWorld(), MaxX, MaxY, MaxZ);

            String ArenaName = "Temp Arena";

            MobToMaterialCollecton mobs = MatToMob(Functions
                    .offsetLocation(p.getLocation(), 0, -1, 0).getBlock()
                    .getType());

            new TempArenaBlock(min, max, ArenaName, Radius, mobs, this,
                    Functions.SecondsToTicks(period));
        }
        else
        {
            p.sendMessage("You can't do this while in an arena!!");
        }
    }

    private boolean ListArenaCommands(Player player, String[] args)
    {
        if (args[0].equalsIgnoreCase("listarenas")
                || args[0].equalsIgnoreCase("la"))
        {
            ListArenas(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("listmobs")
                || args[0].equalsIgnoreCase("lm"))
        {
            ListMobs(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("listlinkedarenas")
                || args[0].equalsIgnoreCase("lla"))
        {
            ListLinkedArenas(player);
            return true;
        }
        return false;
    }

    private boolean EditArenaCommand(Player player, String[] args)
    {
        if (args[0].equalsIgnoreCase("savearena")
                || args[0].equalsIgnoreCase("sa"))
        {
            if (HasArenaBlockSelected(player))
            {
                ArenaBlock ab = GetSelectedArenaBlock(player);
                SaveArenaToConfig(ab);
                player.sendMessage("Arena " + ab.arenaName + " was saved");
                return true;
            }
            else
            {
                return false;
            }
        }
        else if (args[0].equalsIgnoreCase("setwaves")
                || args[0].equalsIgnoreCase("sw"))
        {
            SetWaves(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setarenaspawnarea")
                || args[0].equalsIgnoreCase("sasa"))
        {
            SetArenaSpawnArea(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setarenaspawnarea")
                || args[0].equalsIgnoreCase("sasa"))
        {
            SetArenaSpawnArea(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setarenarewards")
                || args[0].equalsIgnoreCase("sar"))
        {
            SetArenaRewards(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setwavetimer")
                || args[0].equalsIgnoreCase("swt"))
        {
            SetWaveTimer(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setboss")
                || args[0].equalsIgnoreCase("sb"))
        {
            SetBoss(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("remove")
                || args[0].equalsIgnoreCase("r"))
        {
            RemoveArenaBlock(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setplayerblock")
                || args[0].equalsIgnoreCase("spb"))
        {
            SetPlayerBlock(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setwarpblock")
                || args[0].equalsIgnoreCase("swb"))
        {
            SetWarpBlock(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setradius")
                || args[0].equalsIgnoreCase("sr"))
        {
            SetRadius(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setmobtomat")
                || args[0].equalsIgnoreCase("smtm"))
        {
            SetMobToMat(player, args[1], args[2]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("removemobtomat")
                || args[0].equalsIgnoreCase("rmtm"))
        {
            RemoveMobToMat(player, args[1], args[2]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setelitewave")
                || args[0].equalsIgnoreCase("sew"))
        {
            SetEliteWave(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setelitemob")
                || args[0].equalsIgnoreCase("sem"))
        {
            SetEliteMob(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("removeelitemob")
                || args[0].equalsIgnoreCase("rem"))
        {
            RemoveEliteMob(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setdeactivateblock")
                || args[0].equalsIgnoreCase("sdab"))
        {
            SetDeactivateBlock(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setactivateblock")
                || args[0].equalsIgnoreCase("sab"))
        {
            MoveArenaBlock(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setlinkedarenadelay")
                || args[0].equalsIgnoreCase("slad"))
        {
            if (HasArenaBlockSelected(player))
            {
                ArenaBlock selectedAB = GetSelectedArenaBlock(player);
                selectedAB.linkedArenaDelay = Integer.valueOf(args[1]);

                player.sendMessage(selectedAB.arenaName
                        + "s Linked Arena Delay is now " + args[1]);
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("addlinkedarena")
                || args[0].equalsIgnoreCase("ala"))
        {
            if (HasArenaBlockSelected(player))
            {
                ArenaBlock selectedAB = GetSelectedArenaBlock(player);

                ArenaBlock arenaToLink = getArenaBlock(args[1]);

                if (arenaToLink != null)
                {
                    if (arenaToLink != selectedAB)
                    {
                        selectedAB.linkedArenas.add(arenaToLink);

                        // if (StoreLinkedArena(selectedAB.arenaName, args[1]))
                        // {
                        // player.sendMessage(args[1] + " was added to "
                        // + selectedAB.arenaName);
                        // }
                    }
                    else
                    {
                        player.sendMessage("Arenas can't link to themselves");
                    }
                }
                else
                {
                    player.sendMessage("Can't find arena " + args[1]);
                }
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("removelinkedarena")
                || args[0].equalsIgnoreCase("rla"))
        {
            if (HasArenaBlockSelected(player))
            {
                ArenaBlock selectedAB = GetSelectedArenaBlock(player);

                selectedAB.linkedArenas.remove(getArenaBlock(args[1]));
            }
            return true;
        }
        else if (player.hasPermission("BelvensArenas.create")
                && args.length >= 4)
        {
            ArenaBlockCreated(player, player.getLocation().getBlock(), args);
            return true;
        }
        else if (player.hasPermission("BelvensArenas.select")
                && (args[0].equalsIgnoreCase("select") || args[0]
                        .equalsIgnoreCase("s")))
        {
            SelectArena(player, args[1]);
            return true;
        }
        return false;
    }

    public ArenaBlock GetSelectedArenaBlock(Player p)
    {
        return SelectedArenaBlocks.get(p);
    }

    private void ForceStartArena(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            GetSelectedArenaBlock(p).Activate();
        }
    }

    private void SetArenaRewards(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            ab.arenaRewards.clear();

            for (ItemStack is : player.getInventory())
            {
                if (is != null)
                {
                    ab.arenaRewards.add(is);
                }
            }

            player.sendMessage("Arena " + ab.arenaName
                    + " rewards have been set to your invetory.");
        }
    }

    private boolean HasArenaBlockSelected(Player player)
    {
        if (GetSelectedArenaBlock(player) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return false;
        }
        else
            return true;
    }

    private void SetArenaSpawnArea(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);

            WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer()
                    .getPluginManager().getPlugin("WorldEdit");
            Selection sel = worldEdit.getSelection(player);

            if (sel != null)
            {
                if (sel instanceof CuboidSelection)
                {
                    Location min = sel.getMinimumPoint();
                    Location max = sel.getMaximumPoint();
                    ab.spawnAreaStartLocation = min;
                    ab.spawnAreaEndLocation = max;

                    player.sendMessage("Arena " + ab.arenaName
                            + " region has been updated!!");
                }
            }

        }
    }

    public void LeaveArena(Player player)
    {
        if (IsPlayerInArena(player))
        {
            ArenaBlock ab = getArenaInIsPlayer(player);
            ab.arenaPlayers.remove(player);

            PlayersInArenas.remove(player);

            if (ab.arenaPlayers.size() == 0)
            {
                ab.Deactivate();
            }

            if (warpLocations.get(player) != null)
            {
                player.teleport(warpLocations.get(player));
                warpLocations.put(player.getName(), null);

                player.sendMessage("You left the arena " + ab.arenaName
                        + " and have been returned to your last warp");
                return;
            }

            player.sendMessage("You left the arena " + ab.arenaName);
        }
    }

    private void SetDeactivateBlock(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            ab.deactivateBlock = player.getLocation().getBlock();
            player.sendMessage("Arena " + ab.arenaName
                    + " deactivate block has moved!!");
        }
    }

    public boolean IsPlayerInArena(Player p)
    {
        return PlayersInArenas.containsKey(p);
    }

    public ArenaBlock getArenaInIsPlayer(Player p)
    {
        return PlayersInArenas.get(p);
    }

    private void SetEliteMob(Player player, String et)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            player.sendMessage(ab.emc.Set(EntityType.valueOf(et),
                    player.getInventory()));
        }
    }

    private void RemoveEliteMob(Player player, String et)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            player.sendMessage(ab.emc.Remove(EntityType.valueOf(et)));
        }
    }

    private void TeleportArenaMobs(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            for (LivingEntity le : ab.ArenaEntities)
            {
                le.teleport(ab.arenaWarp.getLocation());
            }
        }
    }

    private void ClearArena(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            ab.Deactivate();
            player.sendMessage("Arena " + ab.arenaName + " has been cleared");
        }
    }

    private void SetBoss(Player player, String bossType)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            ab.bm.BossType = EntityType.valueOf(bossType);
            PlayerInventory pi = player.getInventory();
            ab.bm.gear.add(pi.getChestplate());
            ab.bm.gear.add(pi.getHelmet());
            ab.bm.gear.add(pi.getLeggings());
            ab.bm.gear.add(pi.getBoots());
            ab.bm.gear.add(player.getItemInHand());
            player.sendMessage("Arena " + ab.arenaName + " boss is now "
                    + bossType);
        }
    }

    private void SetEliteWave(Player player, String ew)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            ab.eliteWave = Integer.valueOf(ew);
            player.sendMessage("Arena " + ab.arenaName + " elite wave is now "
                    + ew);
        }
    }

    private void SetRadius(Player player, String radius)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            ab.radius = Integer.valueOf(radius);
            player.sendMessage("Arena " + ab.arenaName + " radius is now "
                    + radius);
        }
    }

    private void RemoveMobToMat(Player player, String et, String m)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            player.sendMessage(ab.MobToMat.Remove(et, m));
        }
    }

    private void SetMobToMat(Player player, String et, String m)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = GetSelectedArenaBlock(player);
            player.sendMessage(ab.MobToMat.Add(et, m));
        }
    }

    private void ListLinkedArenas(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            for (ArenaBlock lab : GetSelectedArenaBlock(p).linkedArenas)
            {
                p.sendMessage(lab.arenaName);
            }
        }
    }

    private void ListMobs(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            ArenaBlock ab = GetSelectedArenaBlock(p);

            if (ab != null)
            {
                for (MobToMaterial mtm : ab.MobToMat.MobToMaterials)
                {
                    p.sendMessage(mtm.et.name() + "," + mtm.m.name());
                }
            }
        }
    }

    private void ListArenas(Player player)
    {
        for (ArenaBlock ab : currentArenaBlocks)
        {
            player.sendMessage(ab.arenaName);
        }
    }

    private void SetWaves(Player p, String runtimes)
    {
        if (HasArenaBlockSelected(p))
        {
            GetSelectedArenaBlock(p).maxRunTimes = Integer.valueOf(runtimes);
            p.sendMessage(GetSelectedArenaBlock(p).arenaName + " waves set to "
                    + runtimes);
        }
    }

    public void WarpToArena(Player player, String arenaToWarp)
    {
        ArenaBlock tempArenaBlock = getArenaBlock(arenaToWarp);
        if (tempArenaBlock != null)
        {
            WarpToArena(player, tempArenaBlock);
        }
    }

    public void WarpToArena(Player player, ArenaBlock ab)
    {
        if (!ab.arenaPlayers.contains(player))
        {
            ab.arenaPlayers.add(player);
            PlayersInArenas.put(player, ab);
        }

        if (player.getLocation().getWorld() == ab.LocationToCheckForPlayers
                .getWorld())
        {
            if (player.getLocation().distance(ab.LocationToCheckForPlayers) > ((ab.radius - 2) + (ab.radius / 2)))
            {
                warpLocations.put(player.getName(), player.getLocation());

                player.teleport(Functions.offsetLocation(
                        ab.arenaWarp.getLocation(), 0.5, 0, 0.5));

                player.sendMessage("You teleported to arena " + ab.arenaName);
            }
            else
            {
                player.sendMessage("You were added to arena " + ab.arenaName);
            }
        }
        else
        {
            warpLocations.put(player.getName(), player.getLocation());
            player.teleport(ab.arenaWarp.getLocation());
            player.sendMessage("You teleported to arena " + ab.arenaName);
        }
    }

    private void SetWarpBlock(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            GetSelectedArenaBlock(p).arenaWarp = p.getLocation().getBlock();
            p.sendMessage(GetSelectedArenaBlock(p).arenaName
                    + " warp block set!!");
        }
    }

    private void RemoveArenaBlock(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            String arenaName = GetSelectedArenaBlock(p).arenaName;
            Block ab = GetSelectedArenaBlock(p).blockToActivate;
            ab.removeMetadata("ArenaBlock", this);
            String path = "Arenas." + GetSelectedArenaBlock(p).arenaName;
            getConfig().set(path, null);
            currentArenaBlocks.remove(GetSelectedArenaBlock(p));
            SelectedArenaBlocks.remove(GetSelectedArenaBlock(p));
            p.sendMessage(arenaName + " was removed");
        }
    }

    private void SetWaveTimer(Player p, String newPeriod)
    {
        int period = Functions.SecondsToTicks(Integer.valueOf(newPeriod));

        if (HasArenaBlockSelected(p))
        {
            GetSelectedArenaBlock(p).timerPeriod = period;
            p.sendMessage(GetSelectedArenaBlock(p).arenaName
                    + " mobs now spawn every " + newPeriod);
        }
    }

    private void SetPlayerBlock(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            GetSelectedArenaBlock(p).LocationToCheckForPlayers = p
                    .getLocation();
            p.sendMessage(GetSelectedArenaBlock(p).arenaName
                    + " player block has moved!");
        }
    }

    private void MoveArenaBlock(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            Block tempBlock = GetSelectedArenaBlock(p).blockToActivate;
            tempBlock.removeMetadata("ArenaBlock", this);
            tempBlock = p.getLocation().getBlock();
            tempBlock.setType(Material.REDSTONE_WIRE);
            tempBlock.setMetadata("ArenaBlock", new FixedMetadataValue(this,
                    "Something"));
            GetSelectedArenaBlock(p).blockToActivate = tempBlock;
            p.sendMessage(GetSelectedArenaBlock(p).arenaName
                    + " active block has moved!");
        }
    }

    private void SelectArena(Player p, String arenaToSelect)
    {
        ArenaBlock tempArenaBlock = getArenaBlock(arenaToSelect);
        if (tempArenaBlock != null)
        {
            SelectedArenaBlocks.put(p, tempArenaBlock);
            p.sendMessage(arenaToSelect + " is now selected");
        }
        else
        {
            p.sendMessage("Can't find arena " + arenaToSelect);
        }
    }

    public ArenaBlock getArenaBlock(String arenaToSelect)
    {
        ArenaBlock tempArenaBlock = null;
        for (ArenaBlock ab : currentArenaBlocks)
        {
            if (ab.arenaName.contains(arenaToSelect))
            {
                tempArenaBlock = ab;
                break;
            }
        }
        return tempArenaBlock;
    }

    private void ArenaBlockCreated(Player p, Block block, String[] args)
    {
        block.setMetadata("ArenaBlock", new FixedMetadataValue(this,
                "Something"));

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer()
                .getPluginManager().getPlugin("WorldEdit");
        Selection sel = worldEdit.getSelection(p);

        if (sel != null)
        {
            if (sel instanceof CuboidSelection)
            {
                Location min = sel.getMinimumPoint();
                Location max = sel.getMaximumPoint();

                String ArenaName = args[0];
                int Radius = Integer.valueOf(args[1]);
                MobToMaterialCollecton mobs = MatToMob(Material
                        .getMaterial(args[2]));

                StandardArenaBlock newArenaBlock = new StandardArenaBlock(min,
                        max, ArenaName, Radius, mobs, this,
                        Functions.SecondsToTicks(Integer.valueOf(args[3])));

                SelectedArenaBlocks.put(p, newArenaBlock);
                currentArenaBlocks.add(newArenaBlock);
                p.sendMessage("Arena " + newArenaBlock.arenaName
                        + " was created!!");
            }
        }
        else
        {
            p.sendMessage("Use world edit to select the region");
        }
    }

    private void SaveArenaToConfig(ArenaBlock ab)
    {
        String path = "Arenas." + ab.arenaName;
        getConfig().set(path, null);
        getConfig().set(path + ".Radius", ab.radius);
        getConfig().set(path + ".timerPeriod", ab.timerPeriod);
        getConfig().set(path + ".maxRunTimes", ab.maxRunTimes);

        getConfig().set(path + ".LocationToCheckForPlayers",
                LocationToString(ab.LocationToCheckForPlayers));

        getConfig().set(path + ".World",
                ab.spawnAreaStartLocation.getWorld().getName());

        getConfig().set(path + ".blockToActivate",
                LocationToString(ab.blockToActivate));

        getConfig().set(path + ".deactivateBlock",
                LocationToString(ab.deactivateBlock));

        getConfig().set(path + ".arenaWarp", LocationToString(ab.arenaWarp));

        getConfig().set(path + ".spawnAreaStartLocation",
                LocationToString(ab.spawnAreaStartLocation));

        getConfig().set(path + ".spawnAreaEndLocation",
                LocationToString(ab.spawnAreaEndLocation));

        getConfig().set(path + ".linkedArenaDelay", ab.linkedArenaDelay);
        getConfig().set(path + ".eliteWave", ab.eliteWave);

        SaveArenaEliteMobs(ab);
        SaveArenaMobs(ab);
        SaveArenaRewards(ab);
        getConfig().set(path + ".Boss.Type", ab.bm.BossType.toString());
        saveConfig();
    }

    private void SaveArenaEliteMobs(ArenaBlock ab)
    {
        String path = "Arenas." + ab.arenaName + ".EliteMobs.";

        for (EliteMob em : ab.emc.ems)
        {
            String typePath = path + em.type.name();
            Gear g = em.armor;

            if (g.h != null)
            {
                ItemStackToPath(g.h, typePath + ".Helmet");
            }

            if (g.c != null)
            {
                ItemStackToPath(g.c, typePath + ".ChestPlate");
            }

            if (g.l != null)
            {
                ItemStackToPath(g.l, typePath + ".Leggins");
            }

            if (g.b != null)
            {
                ItemStackToPath(g.b, typePath + ".Boots");
            }

            if (g.w != null)
            {
                ItemStackToPath(g.w, typePath + ".Weapon");
            }
        }
    }

    private void SaveArenaMobs(ArenaBlock ab)
    {
        String path = "Arenas." + ab.arenaName;

        for (Material m : ab.MobToMat.Materials())
        {
            String entities = "";

            for (EntityType et : ab.MobToMat.EntityTypes(m))
            {
                entities += et.toString() + ", ";
            }

            entities = entities.substring(0, entities.length() - 2);
            getConfig().set(path + ".Mobs." + m.toString(), entities);
        }
    }

    private void SaveArenaRewards(ArenaBlock ab)
    {
        String path = "Arenas." + ab.arenaName;

        for (ItemStack is : ab.arenaRewards)
        {
            String currentPath = path + ".Rewards";
            ItemStackToPath(is, currentPath);
        }
    }

    public void ItemStackToPath(ItemStack is, String Path)
    {
        if (is == null)
            return;

        if (is.getType() == Material.POTION)
        {
            SaveItemPotionEffect(is, Path);
        }
        else
        {
            Path += "." + is.getType().toString();
            getConfig().set(Path + ".Amount", is.getAmount());
            getConfig().set(Path + ".Durability", is.getDurability());

            if (is.getEnchantments().size() > 0)
            {
                SaveItemEnchantments(is, Path);
            }
        }
    }

    private void SaveItemPotionEffect(ItemStack is, String currentPath)
    {
        Potion p = Potion.fromItemStack(is);
        currentPath += ".Potions.";
        Collection<PotionEffect> fx = p.getEffects();
        for (PotionEffect pe : fx)
        {
            String path = currentPath + pe.getType().getName();
            getConfig().set(path + ".Amplifier", pe.getAmplifier());
            getConfig().set(path + ".Splash", p.isSplash());
        }
    }

    private void SaveItemEnchantments(ItemStack is, String currentPath)
    {
        Map<Enchantment, Integer> enchants = is.getEnchantments();

        if (!enchants.isEmpty())
        {
            currentPath += ".Enchantments.";

            for (Enchantment e : enchants.keySet())
            {
                getConfig().set(currentPath + e.getName(), enchants.get(e));
            }
        }
    }

    private MobToMaterialCollecton MatToMob(Material mat)
    {
        MobToMaterialCollecton spawnMats = new MobToMaterialCollecton();
        spawnMats.Add(EntityType.ZOMBIE, mat);
        spawnMats.Add(EntityType.SKELETON, mat);
        return spawnMats;
    }

    @Override
    public void onDisable()
    {
        SaveArenas();
    }

    private void SaveArenas()
    {
        for (ArenaBlock ab : currentArenaBlocks)
        {
            SaveArenaToConfig(ab);
        }
    }

    private void ReloadArenaFromConfig(String ArenaName)
    {
        String currentPath = "Arenas.";
        String path = currentPath + ArenaName;

        int radius = getConfig().getInt(path + ".Radius");
        int maxRunTimes = getConfig().getInt(path + ".maxRunTimes");
        int timerPeriod = getConfig().getInt(path + ".timerPeriod");
        int linkedArenaDelay = getConfig().getInt(path + ".linkedArenaDelay");
        int eliteWave = getConfig().getInt(path + ".eliteWave");

        String worldName = getConfig().getString(path + ".World");

        if (worldName == null)
        {
            worldName = "world";
        }

        World world = this.getServer().getWorld(worldName);

        if (world == null)
        {
            WorldCreator wc = new WorldCreator(worldName);
            world = this.getServer().createWorld(wc);

            if (world == null)
            {
                return;
            }
        }

        Location LocationToCheckForPlayers = StringToLocation(getConfig()
                .getString(path + ".LocationToCheckForPlayers"), world);

        Block blockToActivate = StringToLocation(
                getConfig().getString(path + ".blockToActivate"), world)
                .getBlock();

        Block deactivateBlock = StringToLocation(
                getConfig().getString(path + ".deactivateBlock"), world)
                .getBlock();

        Block arenaWarp = StringToLocation(
                getConfig().getString(path + ".arenaWarp"), world).getBlock();

        Location spawnAreaStartLocation = StringToLocation(getConfig()
                .getString(path + ".spawnAreaStartLocation"), world);

        Location spawnAreaEndLocation = StringToLocation(
                getConfig().getString(path + ".spawnAreaEndLocation"), world);

        MobToMaterialCollecton mobs = GetArenaMobs(ArenaName, path);

        StandardArenaBlock newArenaBlock = new StandardArenaBlock(
                spawnAreaStartLocation, spawnAreaEndLocation, ArenaName,
                radius, mobs, this, timerPeriod);

        blockToActivate.setMetadata("ArenaBlock", new FixedMetadataValue(this,
                "Something"));

        newArenaBlock.arenaWarp = arenaWarp;
        newArenaBlock.arenaRewards = GetArenaRewards(ArenaName, path);
        newArenaBlock.blockToActivate = blockToActivate;
        newArenaBlock.LocationToCheckForPlayers = LocationToCheckForPlayers;
        newArenaBlock.deactivateBlock = deactivateBlock;
        newArenaBlock.linkedArenaDelay = linkedArenaDelay;
        newArenaBlock.maxRunTimes = maxRunTimes;
        newArenaBlock.eliteWave = eliteWave;
        currentArenaBlocks.add(newArenaBlock);
        getLogger().info(ArenaName + " has been created!!");
    }

    private void RecreateArenasFromConfig()
    {
        String currentPath = "Arenas.";
        Set<String> arenas = getConfig().getConfigurationSection(currentPath)
                .getKeys(false);

        for (String ArenaName : arenas)
        {
            ReloadArenaFromConfig(ArenaName);
        }
    }

    private MobToMaterialCollecton GetArenaMobs(String ArenaName, String Path)
    {
        MobToMaterialCollecton tempMobs = new MobToMaterialCollecton();
        Set<String> materials = getConfig().getConfigurationSection(
                Path + ".Mobs").getKeys(false);

        for (String mat : materials)
        {
            String[] mobs = getConfig().getString(Path + ".Mobs." + mat).split(
                    ", ");

            for (String et : mobs)
            {
                tempMobs.Add(EntityType.valueOf(et), Material.valueOf(mat));
            }
        }

        return tempMobs;
    }

    private List<ItemStack> GetItemsAtPath(String Path)
    {
        List<ItemStack> tempItems = new ArrayList<ItemStack>();

        Set<String> items = getConfig().getConfigurationSection(Path).getKeys(
                false);

        for (String item : items)
        {
            if (item.equals("Potions"))
            {
                ConfigurationSection potionsConfig = getConfig()
                        .getConfigurationSection(Path + ".Potions");
                if (potionsConfig != null)
                {
                    for (String pe : potionsConfig.getKeys(false))
                    {
                        tempItems.add(GetItemFromPath(pe, Path));
                    }
                }
            }
            else
            {
                tempItems.add(GetItemFromPath(item, Path));
            }
        }
        return tempItems;

    }

    private ItemStack GetItemFromPath(String item, String Path)
    {
        ItemStack currentItem;
        String itemPath = Path + item;

        Material mat = Material.getMaterial(item);

        if (mat != null)
        {
            int amount = getConfig().getInt(itemPath + ".Amount");
            int durability = getConfig().getInt(itemPath + ".Durability");
            currentItem = new ItemStack(mat, amount);
            currentItem.setDurability((short) durability);

            ConfigurationSection enchantsConfig = getConfig()
                    .getConfigurationSection(itemPath + ".Enchantments");

            if (enchantsConfig != null)
            {
                AddItemEnchantments(currentItem, itemPath);
            }
            return currentItem;
        }
        return AddPotionFromConfig(item, Path);
    }

    private void AddItemEnchantments(ItemStack is, String Path)
    {
        Set<String> enchants = getConfig().getConfigurationSection(
                Path + ".Enchantments").getKeys(false);

        for (String et : enchants)
        {
            String enchantPath = Path + ".Enchantments." + et;
            int level = getConfig().getInt(enchantPath);
            Enchantment e = Enchantment.getByName(et);
            is.addEnchantment(e, level);
        }
    }

    private List<ItemStack> GetArenaRewards(String ArenaName, String Path)
    {
        if (getConfig().getConfigurationSection(Path + ".Rewards") == null)
        {
            return new ArrayList<ItemStack>();
        }
        return GetItemsAtPath(Path + ".Rewards");
    }

    @SuppressWarnings("deprecation")
    private ItemStack AddPotionFromConfig(String pe, String itemPath)
    {
        PotionEffectType pet = PotionEffectType.getByName(pe);

        if (pet == null)
            return null;

        PotionType pt = PotionType.getByEffect(pet);

        if (pt != null)
        {
            String potionPath = itemPath + ".Potions." + pe;
            int Amplifier = getConfig().getInt(potionPath + ".Amplifier");
            boolean Splash = getConfig().getBoolean(potionPath + ".Splash");

            Potion p = new Potion(pt, Amplifier == 0 ? 1 : Amplifier, Splash);

            return p.toItemStack(1);
        }
        return null;
    }

    private Location StringToLocation(String s, World world)
    {
        Location tempLoc;
        String[] strings = s.split(",");
        int x = Integer.valueOf(strings[0].trim());
        int y = Integer.valueOf(strings[1].trim());
        int z = Integer.valueOf(strings[2].trim());
        tempLoc = new Location(world, x, y, z);
        return tempLoc;
    }

    private String LocationToString(Block block)
    {
        String locationString = "";
        Location l = block.getLocation();

        locationString = String.valueOf(l.getBlockX()) + ","
                + String.valueOf(l.getBlockY()) + ","
                + String.valueOf(l.getBlockZ());
        return locationString;
    }

    private String LocationToString(Location l)
    {
        String locationString = "";

        if (l != null)
        {
            locationString = String.valueOf(l.getBlockX()) + ","
                    + String.valueOf(l.getBlockY()) + ","
                    + String.valueOf(l.getBlockZ());
        }
        return locationString;
    }
}