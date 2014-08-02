package belven.arena;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import resources.Functions;
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
    public HashMap<String, ArenaBlock> SelectedArenaBlocks = new HashMap<String, ArenaBlock>();

    private HashMap<Player, ArenaBlock> PlayersInArenas = new HashMap<Player, ArenaBlock>();
    public HashMap<String, Location> warpLocations = new HashMap<String, Location>();

    private static String queryStringSep = "', '";
    private static String queryNumberSep = ", ";
    private static String queryStringToNumberSep = "', ";
    // private static String queryNumberToStringSep = ", '";

    public TeamManager teams = (TeamManager) Bukkit.getServer()
            .getPluginManager().getPlugin("BelvensTeams");

    String connectionUrl = "jdbc:sqlserver://f0bh84aran.database.windows.net:1433;database=Arenas;user=belven@f0bh84aran;password=Something123;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    @Override
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(newplayerListener, this);
        pm.registerEvents(blockListener, this);
        pm.registerEvents(arenaListener, this);
        pm.registerEvents(mobListener, this);
        // getServer().getName();
        RecreateArenas();
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

    private boolean UtilityArenaCommands(Player player, String[] args)
    {
        if (args[0].equalsIgnoreCase("cleararena")
                || args[0].equalsIgnoreCase("ca"))
        {
            ClearArena(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("forcestart")
                || args[0].equalsIgnoreCase("fs"))
        {
            ForceStartArena(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("portmobs")
                || args[0].equalsIgnoreCase("pm"))
        {
            TeleportArenaMobs(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("warp")
                || args[0].equalsIgnoreCase("w"))
        {
            WarpToArena(player, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("createtemparena")
                || args[0].equalsIgnoreCase("cta"))
        {
            CreateTempArena(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("leave")
                || args[0].equalsIgnoreCase("l"))
        {
            LeaveArena(player);
            return true;
        }

        return false;
    }

    private void CreateTempArena(Player p)
    {
        if (!IsPlayerInArena(p))
        {
            int maxSize = 0;
            int Radius = 40;
            int period = 0;

            Location pLoc = p.getLocation();

            Player[] tempPlayers = Functions.getNearbyPlayersNew(pLoc,
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

            String ArenaName = "Temp Arena"; // UUID.randomUUID().toString();

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
                ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
                UpdateArenaNew(ab);
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
                ArenaBlock selectedAB = SelectedArenaBlocks.get(player
                        .getName());
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
                ArenaBlock selectedAB = SelectedArenaBlocks.get(player
                        .getName());

                ArenaBlock arenaToLink = getArenaBlock(args[1]);

                if (arenaToLink != null)
                {
                    if (arenaToLink != selectedAB)
                    {
                        selectedAB.linkedArenas.add(arenaToLink);

                        if (StoreLinkedArena(selectedAB.arenaName, args[1]))
                        {
                            player.sendMessage(args[1] + " was added to "
                                    + selectedAB.arenaName);
                        }
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
                ArenaBlock selectedAB = SelectedArenaBlocks.get(player
                        .getName());

                selectedAB.linkedArenas.remove(getArenaBlock(args[1]));

                if (RemoveLinkedArena(selectedAB, args[1]))
                {
                    player.sendMessage(args[1] + " was removed from "
                            + selectedAB.arenaName);
                }
            }
            return true;
        }
        else if (player.hasPermission("BelvensArenas.create")
                && args.length >= 4)
        {
            try
            {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                con = DriverManager.getConnection(connectionUrl);
                stmt = con.createStatement();

                rs = stmt.executeQuery("ArenaExists '" + args[0] + "'");

                if (rs.next()
                        && rs.getString("ArenaExists").equalsIgnoreCase("1"))
                {
                    player.sendMessage("Arena " + args[0] + " already exists");
                }
                else
                {
                    ArenaBlockCreated(player, player.getLocation().getBlock(),
                            args);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }

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

    private void ForceStartArena(Player p)
    {
        if (HasArenaBlockSelected(p))
        {
            SelectedArenaBlocks.get(p.getName()).Activate();
        }
    }

    private void SetArenaRewards(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
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
        if (SelectedArenaBlocks.get(player.getName()) == null)
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
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());

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

            if (warpLocations.get(player.getName()) != null)
            {
                player.teleport(warpLocations.get(player.getName()));
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
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
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
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            player.sendMessage(ab.emc.Set(EntityType.valueOf(et),
                    player.getInventory()));
        }
    }

    private void RemoveEliteMob(Player player, String et)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            player.sendMessage(ab.emc.Remove(EntityType.valueOf(et)));
        }
    }

    private void TeleportArenaMobs(Player player)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
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
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.Deactivate();
            player.sendMessage("Arena " + ab.arenaName + " has been cleared");
        }
    }

    private void SetBoss(Player player, String bossType)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
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
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.eliteWave = Integer.valueOf(ew);
            player.sendMessage("Arena " + ab.arenaName + " elite wave is now "
                    + ew);
        }
    }

    private void SetRadius(Player player, String radius)
    {
        if (HasArenaBlockSelected(player))
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.radius = Integer.valueOf(radius);
            player.sendMessage("Arena " + ab.arenaName + " radius is now "
                    + radius);
        }
    }

    private void RemoveMobToMat(Player player, String et, String m)
    {
        if (HasArenaBlockSelected(player))
        {
            try
            {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                con = DriverManager.getConnection(connectionUrl);

                stmt = con.createStatement();

                ArenaBlock ab = SelectedArenaBlocks.get(player.getName());

                String mobToRemove = "";

                mobToRemove = mobToRemove + ab.arenaName + "', '";
                mobToRemove = mobToRemove + et + "', '";
                mobToRemove = mobToRemove + m + "'";

                stmt.executeUpdate("RemoveArenaMob '" + mobToRemove);
                player.sendMessage(ab.MobToMat.Remove(et, m));
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void SetMobToMat(Player player, String et, String m)
    {
        if (HasArenaBlockSelected(player))
        {
            try
            {
                ArenaBlock ab = SelectedArenaBlocks.get(player.getName());

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                con = DriverManager.getConnection(connectionUrl);

                stmt = con.createStatement();
                String mobToAdd = "";

                mobToAdd = mobToAdd + ab.arenaName + "', '";
                mobToAdd = mobToAdd + et + "', '";
                mobToAdd = mobToAdd + m + "'";

                stmt.executeUpdate("InsertMobToMat '" + mobToAdd);
                player.sendMessage(ab.MobToMat.Add(et, m));
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void ListLinkedArenas(Player currentPlayer)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            for (ArenaBlock lab : SelectedArenaBlocks.get(currentPlayer
                    .getName()).linkedArenas)
            {
                currentPlayer.sendMessage(lab.arenaName);
            }
        }
    }

    private void ListMobs(Player currentPlayer)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            ArenaBlock ab = SelectedArenaBlocks.get(currentPlayer.getName());

            if (ab != null)
            {
                for (MobToMaterial mtm : ab.MobToMat.MobToMaterials)
                {
                    currentPlayer.sendMessage(mtm.et.name() + ","
                            + mtm.m.name());
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

    private void SetWaves(Player currentPlayer, String runtimes)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).maxRunTimes = Integer
                    .valueOf(runtimes);
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " waves set to " + runtimes);
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

    private void SetWarpBlock(Player currentPlayer)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).arenaWarp = currentPlayer
                    .getLocation().getBlock();
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " warp block set!!");
        }
    }

    private void RemoveArenaBlock(Player currentPlayer)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            String arenaName = SelectedArenaBlocks.get(currentPlayer.getName()).arenaName;
            Block ab = SelectedArenaBlocks.get(currentPlayer.getName()).blockToActivate;
            ab.removeMetadata("ArenaBlock", this);

            RemoveArena(SelectedArenaBlocks.get(currentPlayer.getName()));

            currentArenaBlocks.remove(SelectedArenaBlocks.get(currentPlayer
                    .getName()));

            SelectedArenaBlocks.remove(SelectedArenaBlocks.get(currentPlayer
                    .getName()));

            currentPlayer.sendMessage(arenaName + " was removed");
        }
    }

    private void SetWaveTimer(Player currentPlayer, String newPeriod)
    {
        int period = Functions.SecondsToTicks(Integer.valueOf(newPeriod));

        if (HasArenaBlockSelected(currentPlayer))
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).timerPeriod = period;
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName
                    + " mobs now spawn every "
                    + newPeriod);
        }
    }

    private void SetPlayerBlock(Player currentPlayer)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).LocationToCheckForPlayers = currentPlayer
                    .getLocation();
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " player block has moved!");
        }
    }

    private void MoveArenaBlock(Player currentPlayer)
    {
        if (HasArenaBlockSelected(currentPlayer))
        {
            Block tempBlock = SelectedArenaBlocks.get(currentPlayer.getName()).blockToActivate;
            tempBlock.removeMetadata("ArenaBlock", this);

            tempBlock = currentPlayer.getLocation().getBlock();
            tempBlock.setType(Material.REDSTONE_WIRE);
            tempBlock.setMetadata("ArenaBlock", new FixedMetadataValue(this,
                    "Something"));
            SelectedArenaBlocks.get(currentPlayer.getName()).blockToActivate = tempBlock;
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " active block has moved!");
        }
    }

    private void SelectArena(Player currentPlayer, String arenaToSelect)
    {
        ArenaBlock tempArenaBlock = getArenaBlock(arenaToSelect);
        if (tempArenaBlock != null)
        {
            SelectedArenaBlocks.put(currentPlayer.getName(), tempArenaBlock);
            currentPlayer.sendMessage(arenaToSelect + " is now selected");
        }
        else
        {
            currentPlayer.sendMessage("Can't find arena " + arenaToSelect);
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

    private void ArenaBlockCreated(Player currentPlayer, Block block,
            String[] args)
    {
        block.setMetadata("ArenaBlock", new FixedMetadataValue(this,
                "Something"));

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer()
                .getPluginManager().getPlugin("WorldEdit");
        Selection sel = worldEdit.getSelection(currentPlayer);

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

                SelectedArenaBlocks.put(currentPlayer.getName(), newArenaBlock);
                currentArenaBlocks.add(newArenaBlock);
                currentPlayer.sendMessage("Arena " + newArenaBlock.arenaName
                        + " was created!!");

                InsertArena(newArenaBlock);
            }
        }
        else
        {
            currentPlayer.sendMessage("Use world edit to select the region");
        }
    }

    private MobToMaterialCollecton MatToMob(String ArenaName, Statement stmt,
            ResultSet rs)
    {
        MobToMaterialCollecton spawnMats = new MobToMaterialCollecton();

        try
        {
            stmt = con.createStatement();

            rs = stmt.executeQuery("GetArenaMobs '" + ArenaName + "'");

            while (rs.next())
            {
                EntityType et = EntityType.valueOf(rs.getString("EntityType"));
                Material m = Material.getMaterial(rs.getString("Material"));

                if (m == null)
                {
                    m = Material.STONE;
                }

                if (et == null)
                {
                    et = EntityType.ZOMBIE;
                }

                spawnMats.Add(et, m);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return spawnMats;
    }

    private void UpdateArenaNew(ArenaBlock ab)
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);

            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);

            ResultSet rs = GetArenaResult(ab.arenaName, stmt);

            if (rs != null)
            {
                while (!rs.isClosed() && rs.next())
                {
                    rs.updateString("BlockToActivate",
                            LocationToString(ab.blockToActivate));

                    rs.updateString("DeactivateBlock",
                            LocationToString(ab.deactivateBlock));

                    rs.updateString("ArenaWarp", LocationToString(ab.arenaWarp));

                    rs.updateString("ArenaBlockStartLocation",
                            LocationToString(ab.spawnAreaStartLocation));

                    rs.updateString("LocationToCheckForPlayers",
                            LocationToString(ab.LocationToCheckForPlayers));

                    rs.updateInt("Radius", ab.radius);

                    rs.updateInt("TimerPeriod", ab.timerPeriod);

                    rs.updateInt("MaxRunTimes", ab.maxRunTimes);

                    rs.updateString("World", ab.blockToActivate.getWorld()
                            .getName());

                    rs.updateString("ArenaBlockEndLocation",
                            LocationToString(ab.spawnAreaEndLocation));

                    rs.updateInt("LinkedArenaDelay", ab.linkedArenaDelay);

                    rs.updateRow();

                    for (MobToMaterial mtm : ab.MobToMat.MobToMaterials)
                    {
                        String mobExists = "";
                        String Insert = "InsertMobToMat '";

                        mobExists = mobExists + ab.arenaName + "', '";
                        mobExists = mobExists + mtm.et.name() + "', '";
                        mobExists = mobExists + mtm.m.name() + "'";

                        rs = stmt.executeQuery("ArenaMobsExist '" + mobExists);

                        if (rs.next()
                                && rs.getString("MobExists").equalsIgnoreCase(
                                        "0"))
                        {
                            stmt.executeUpdate(Insert + mobExists);
                        }
                    }

                    String resetArenaItems = "ResetArenaItems '" + ab.arenaName
                            + "'";

                    stmt.executeUpdate(resetArenaItems);

                    for (ItemStack is : ab.arenaRewards)
                    {
                        String mobExists = "";
                        String Insert = "AddArenaReward '";

                        mobExists = mobExists + ab.arenaName + queryStringSep;
                        mobExists = mobExists + is.getType().name()
                                + queryStringSep;
                        mobExists = mobExists
                                + String.valueOf((int) is.getDurability())
                                + queryStringToNumberSep;
                        mobExists = mobExists + is.getAmount() + queryNumberSep;
                        mobExists = mobExists + 1;

                        stmt.executeUpdate(Insert + mobExists);
                    }

                    RemovedAllLinkedArenas(ab);
                    StoreAllLinkedArenas(ab);

                    getLogger().info(
                            ab.arenaName + " has been save to the database!!");
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private ResultSet GetArenaResult(String ArenaName, Statement stmt)
    {
        try
        {
            ResultSet rs = stmt
                    .executeQuery("SELECT * FROM arena WHERE ArenaName LIKE '"
                            + ArenaName + "'");
            return rs;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private List<ItemStack> GetArenaRewards(String ArenaName, Statement stmt,
            ResultSet rs)
    {
        ArrayList<ItemStack> tempRewards = new ArrayList<ItemStack>();
        try
        {
            stmt = con.createStatement();

            rs = stmt.executeQuery("GetArenaItems '" + ArenaName + "'");

            while (rs.next())
            {
                Material m = Material.valueOf(rs.getString("MaterialType"));
                int a = rs.getInt("Amount");
                ItemStack is = new ItemStack(m, a);
                is.setDurability(rs.getShort("Durability"));
                tempRewards.add(is);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return tempRewards;
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
        getLogger().info("Goodbye world!");
        SaveArenas();
    }

    private void InsertArena(ArenaBlock ab)
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);

            String Insert = "InsertArena ";

            stmt = con.createStatement();

            PreparedStatement ps = con
                    .prepareStatement("INSERT INTO arena VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            ps.setString(1, ab.arenaName);
            ps.setString(2, LocationToString(ab.blockToActivate));
            ps.setString(3, LocationToString(ab.deactivateBlock));
            ps.setString(4, LocationToString(ab.arenaWarp));
            ps.setString(5, LocationToString(ab.spawnAreaStartLocation));
            ps.setString(6, LocationToString(ab.LocationToCheckForPlayers));
            ps.setInt(7, ab.radius);
            ps.setString(8, "Grass");
            ps.setInt(10, ab.timerPeriod);
            ps.setInt(11, ab.maxRunTimes);
            ps.setString(12, ab.blockToActivate.getWorld().getName());
            ps.setString(13, LocationToString(ab.spawnAreaEndLocation));
            ps.setInt(14, ab.linkedArenaDelay);
            ps.addBatch();
            ps.executeBatch();

            // stmt.executeUpdate(Insert + ArenaString(ab));

            for (MobToMaterial mtm : ab.MobToMat.MobToMaterials)
            {
                String mobExists = "";
                Insert = "InsertMobToMat '";

                mobExists = mobExists + ab.arenaName + "', '";
                mobExists = mobExists + mtm.et.name() + "', '";
                mobExists = mobExists + mtm.m.name() + "'";

                rs = stmt.executeQuery("ArenaMobsExist '" + mobExists);

                if (rs.next()
                        && rs.getString("MobExists").equalsIgnoreCase("0"))
                {
                    stmt.executeUpdate(Insert + mobExists);
                }
            }

            String resetArenaItems = "ResetArenaItems '" + ab.arenaName + "'";
            stmt.executeUpdate(resetArenaItems);

            for (ItemStack is : ab.arenaRewards)
            {
                String mobExists = "";
                Insert = "AddArenaReward '";

                mobExists = mobExists + ab.arenaName + queryStringSep;
                mobExists = mobExists + is.getType().name() + queryStringSep;
                mobExists = mobExists
                        + String.valueOf((int) is.getDurability())
                        + queryStringToNumberSep;
                mobExists = mobExists + is.getAmount() + queryNumberSep;
                mobExists = mobExists + 1;

                stmt.executeUpdate(Insert + mobExists);
            }

            RemovedAllLinkedArenas(ab);
            StoreAllLinkedArenas(ab);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (stmt != null)
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
            }
        if (con != null)
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
    }

    private void RemoveArena(ArenaBlock ab)
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            stmt = con.createStatement();

            RemovedAllLinkedArenas(ab);

            stmt.executeUpdate("RemoveArena '" + ab.arenaName + "'");
        }
        catch (ClassNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        if (stmt != null)
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
            }
        if (con != null)
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
    }

    private void SaveArenas()
    {
        for (ArenaBlock ab : currentArenaBlocks)
        {
            UpdateArenaNew(ab);
        }
    }

    private void RecreateArenas()
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Arena");

            while (rs.next())
            {
                String ArenaName = rs.getString("ArenaName");
                String warpLocation = rs.getString("ArenaWarp");

                int Radius = rs.getInt("Radius");
                int TimerPeriod = rs.getInt("TimerPeriod");
                int MaxRunTimes = rs.getInt("MaxRunTimes");

                World world = this.getServer().getWorld(rs.getString("World"));

                if (world == null)
                {
                    WorldCreator wc = new WorldCreator(rs.getString("World"));
                    world = this.getServer().createWorld(wc);
                }

                if (world != null)
                {
                    Block BlockToActivate = StringToLocation(
                            rs.getString("BlockToActivate"), world).getBlock();

                    Location arenaBlockStartLocation = StringToLocation(
                            rs.getString("ArenaBlockStartLocation"), world);

                    Location arenaBlockEndLocation = StringToLocation(
                            rs.getString("ArenaBlockEndLocation"), world);

                    Location LocationToCheckForPlayers = StringToLocation(
                            rs.getString("LocationToCheckForPlayers"), world);

                    Block BlockToDeactivate = StringToLocation(
                            rs.getString("DeactivateBlock"), world).getBlock();

                    MobToMaterialCollecton mobs = MatToMob(ArenaName, stmt, rs);

                    int LinkedArenaDelay = rs.getInt("LinkedArenaDelay");

                    StandardArenaBlock newArenaBlock = new StandardArenaBlock(
                            arenaBlockStartLocation, arenaBlockEndLocation,
                            ArenaName, Radius, mobs, this, TimerPeriod);

                    newArenaBlock.arenaRewards = GetArenaRewards(ArenaName,
                            stmt, rs);

                    BlockToActivate.setMetadata("ArenaBlock",
                            new FixedMetadataValue(this, "Something"));

                    newArenaBlock.arenaWarp = StringToLocation(warpLocation,
                            world).getBlock();

                    newArenaBlock.blockToActivate = BlockToActivate;
                    newArenaBlock.LocationToCheckForPlayers = LocationToCheckForPlayers;
                    newArenaBlock.deactivateBlock = BlockToDeactivate;
                    newArenaBlock.linkedArenaDelay = LinkedArenaDelay;
                    newArenaBlock.maxRunTimes = Integer.valueOf(MaxRunTimes);
                    currentArenaBlocks.add(newArenaBlock);
                }
            }

            for (ArenaBlock ab : currentArenaBlocks)
            {
                for (ArenaBlock lab : GetLinkedArenas(ab.arenaName, stmt, rs))
                {
                    ab.linkedArenas.add(lab);
                }
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private List<ArenaBlock> GetLinkedArenas(String arenaName, Statement stmt,
            ResultSet rs)
    {
        List<ArenaBlock> tempArenas = new ArrayList<ArenaBlock>();
        ArenaBlock tempArena = null;
        try
        {
            rs = stmt
                    .executeQuery("SELECT * FROM linkedarenas WHERE Parent like '"
                            + arenaName + "'");

            while (rs.next())
            {
                String ArenaName = rs.getString("Child");
                tempArena = getArenaBlock(ArenaName);

                if (tempArena != null)
                {
                    tempArenas.add(tempArena);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return tempArenas;
    }

    private void StoreAllLinkedArenas(ArenaBlock ab)
    {
        for (ArenaBlock lab : ab.linkedArenas)
        {
            if (lab != null)
            {
                StoreLinkedArena(ab.arenaName, lab.arenaName);
            }
        }
    }

    private boolean StoreLinkedArena(String arenaName, String linkedArena)
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            stmt = con.createStatement();

            stmt.executeUpdate("AddLinkedArena '" + arenaName + "'" + ", '"
                    + linkedArena + "'");
            return true;

        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private void RemovedAllLinkedArenas(ArenaBlock ab)
    {
        for (ArenaBlock lab : ab.linkedArenas)
        {
            RemoveLinkedArena(ab, lab.arenaName);
        }
    }

    private boolean RemoveLinkedArena(ArenaBlock ab, String linkedArena)
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            stmt = con.createStatement();

            stmt.executeUpdate("RemoveLinkedArena '" + ab.arenaName + "'"
                    + ", '" + linkedArena + "'");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
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
