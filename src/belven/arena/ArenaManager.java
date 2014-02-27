package belven.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import belven.arena.blocks.ArenaBlock;
import belven.arena.listeners.ArenaListener;
import belven.arena.listeners.BlockListener;
import belven.arena.listeners.MobListener;
import belven.arena.listeners.PlayerListener;
import belven.arena.resources.functions;

public class ArenaManager extends JavaPlugin
{
    private final PlayerListener newplayerListener = new PlayerListener(this);
    private final BlockListener blockListener = new BlockListener(this);
    private final ArenaListener arenaListener = new ArenaListener(this);
    private final MobListener mobListener = new MobListener(this);

    public List<ArenaBlock> currentArenaBlocks = new ArrayList<ArenaBlock>();
    public HashMap<String, ArenaBlock> SelectedArenaBlocks = new HashMap<String, ArenaBlock>();
    // private Database sql;
    private static String queryStringSep = "', '";
    private static String queryNumberSep = ", ";
    private static String queryStringToNumberSep = "', ";
    private static String queryNumberToStringSep = ", '";

    @Override
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(newplayerListener, this);
        pm.registerEvents(blockListener, this);
        pm.registerEvents(arenaListener, this);
        pm.registerEvents(mobListener, this);

        // try
        // {
        // sql = new MicrosoftSQL(Logger.getLogger("Minecraft"), "Something",
        // "f0bh84aran.database.windows.net", 1433, "arenas",
        // "belven", "Something123");
        // }
        // catch (SQLException e)
        // {
        // e.printStackTrace();
        // }
        //
        // sql.open();

        RecreateArenas();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args)
    {
        Player player = (Player) sender;
        String commandSent = cmd.getName();

        if (commandSent.equalsIgnoreCase("ba"))
        {
            if (args[0].equalsIgnoreCase("select")
                    || args[0].equalsIgnoreCase("s"))
            {
                SelectArena(player, args[1]);
                return true;
            }
            else if (args[0].equalsIgnoreCase("quickarena")
                    || args[0].equalsIgnoreCase("qa"))
            {
                ArenaBlockCreated(player, player.getLocation().getBlock(),
                        args[1]);
                return true;
            }
            else if (args[0].equalsIgnoreCase("cleararena")
                    || args[0].equalsIgnoreCase("ca"))
            {
                ClearArena(player);
                return true;
            }
            else if (args[0].equalsIgnoreCase("setactivateblock")
                    || args[0].equalsIgnoreCase("sab"))
            {
                MoveArenaBlock(player);
                return true;
            }
            else if (args[0].equalsIgnoreCase("portmobs")
                    || args[0].equalsIgnoreCase("pm"))
            {
                TeleportArenaMobs(player);
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
            else if (args[0].equalsIgnoreCase("warp")
                    || args[0].equalsIgnoreCase("w"))
            {
                WarpToArena(player, args[1]);
                return true;
            }
            else if (args[0].equalsIgnoreCase("setboss")
                    || args[0].equalsIgnoreCase("sb"))
            {
                SetBoss(player, args[1]);
                return true;
            }
            else if (args[0].equalsIgnoreCase("list")
                    || args[0].equalsIgnoreCase("l"))
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
            else if (args[0].equalsIgnoreCase("setwavetimer")
                    || args[0].equalsIgnoreCase("swt"))
            {
                SetWaveTimer(player, args[1]);
                return true;
            }
            else if (args[0].equalsIgnoreCase("setwaves")
                    || args[0].equalsIgnoreCase("sw"))
            {
                SetWaves(player, args[1]);
                return true;
            }
            else if (args.length >= 4)
            {
                ArenaBlockCreated(player, player.getLocation().getBlock(), args);
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

    private void TeleportArenaMobs(Player player)
    {
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
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
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.Deactivate();
            player.sendMessage("Arena " + ab.arenaName + " has been cleared");
        }
    }

    private void SetBoss(Player player, String bossType)
    {
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.bm.BossType = EntityType.valueOf(bossType);
            player.sendMessage("Arena " + ab.arenaName + " boss is now "
                    + bossType);
        }
    }

    private void SetEliteWave(Player player, String ew)
    {
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.eliteWave = Integer.valueOf(ew);
            player.sendMessage("Arena " + ab.arenaName + " elite wave is now "
                    + ew);
        }
    }

    private void SetRadius(Player player, String radius)
    {
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.radius = Integer.valueOf(radius);
            player.sendMessage("Arena " + ab.arenaName + " radius is now "
                    + radius);
        }
    }

    private void RemoveMobToMat(Player player, String et, String m)
    {
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            player.sendMessage(ab.MobToMat.Remove(et, m));
        }

    }

    private void SetMobToMat(Player player, String et, String m)
    {
        if (SelectedArenaBlocks.get(player.getName()) == null)
        {
            player.sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            ArenaBlock ab = SelectedArenaBlocks.get(player.getName());
            ab.MobToMat.Add(et, m);
        }
    }

    private void ListMobs(Player currentPlayer)
    {
        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
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
        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).maxRunTimes = Integer
                    .valueOf(runtimes);
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " waves set to " + runtimes);
        }
    }

    private void WarpToArena(Player player, String arenaToWarp)
    {
        ArenaBlock tempArenaBlock = getArenaBlock(arenaToWarp);
        if (tempArenaBlock != null)
        {
            player.teleport(tempArenaBlock.arenaWarp.getLocation());
            player.sendMessage("Teleport to " + arenaToWarp);
        }
        else
        {
            player.sendMessage("Can't find arena " + arenaToWarp);
        }
    }

    private void SetWarpBlock(Player currentPlayer)
    {
        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).arenaWarp = currentPlayer
                    .getLocation().getBlock();
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " warp block set!!");
        }
    }

    private void RemoveArenaBlock(Player currentPlayer)
    {
        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            String arenaName = SelectedArenaBlocks.get(currentPlayer.getName()).arenaName;
            Block tempBlock = SelectedArenaBlocks.get(currentPlayer.getName()).blockToActivate;
            tempBlock.removeMetadata("ArenaBlock", this);
            currentArenaBlocks.remove(SelectedArenaBlocks.get(currentPlayer
                    .getName()));
            currentPlayer.sendMessage(arenaName + " was removed");
        }
    }

    private void SetWaveTimer(Player currentPlayer, String newPeriod)
    {
        int period = functions.SecondsToTicks(Integer.valueOf(newPeriod));

        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
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
        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            SelectedArenaBlocks.get(currentPlayer.getName()).LocationToCheckForPlayers = currentPlayer
                    .getLocation();
            currentPlayer.sendMessage(SelectedArenaBlocks.get(currentPlayer
                    .getName()).arenaName + " player block has moved!");
        }
    }

    private void MoveArenaBlock(Player currentPlayer)
    {
        if (SelectedArenaBlocks.get(currentPlayer.getName()) == null)
        {
            currentPlayer
                    .sendMessage("Please select an Arena using /ba select <ArenaName>");
            return;
        }
        else
        {
            Block tempBlock = SelectedArenaBlocks.get(currentPlayer.getName()).blockToActivate;
            tempBlock.removeMetadata("ArenaBlock", this);

            tempBlock = currentPlayer.getLocation().getBlock();
            tempBlock.setType(Material.LEVER);
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

    public void ArenaBlockCreated(Player currentPlayer, Block block,
            String[] args)
    {
        block.setType(Material.REDSTONE_WIRE);
        block.setMetadata("ArenaBlock", new FixedMetadataValue(this,
                "Something"));

        ArenaBlock newArenaBlock = new ArenaBlock(block, args[0],
                Integer.valueOf(args[1]), MatToMob(args[0],
                        Material.getMaterial(args[2])), this,
                functions.SecondsToTicks(1), functions.SecondsToTicks(Integer
                        .valueOf(args[3])));

        SelectedArenaBlocks.put(currentPlayer.getName(), newArenaBlock);
        currentArenaBlocks.add(newArenaBlock);
        currentPlayer.sendMessage("Arena " + newArenaBlock.arenaName
                + " was created!!");
    }

    public void ArenaBlockCreated(Player currentPlayer, Block block, String mat)
    {
        List<String> args = new ArrayList<String>();
        String arenaName = "Test";
        String radius = "10";
        String timerPeriod = "10";

        args.add(arenaName);
        args.add(radius);
        args.add(mat);
        args.add(timerPeriod);
        ArenaBlockCreated(currentPlayer, block, args.toArray(new String[5]));
    }

    public MobToMaterialCollecton MatToMob(String ArenaName, Material mat)
    {
        MobToMaterialCollecton spawnMats = new MobToMaterialCollecton();
        spawnMats.Add(EntityType.ZOMBIE, mat);
        spawnMats.Add(EntityType.SKELETON, mat);
        return spawnMats;
    }

    public boolean ArenaExists(String ArenaName)
    {
        boolean tempArenaExists = false;
        if (currentArenaBlocks.size() > 0)
        {
            for (ArenaBlock ab : currentArenaBlocks)
            {
                if (ab.arenaName == ArenaName)
                {
                    tempArenaExists = true;
                    break;
                }
            }
        }
        return tempArenaExists;
    }

    @Override
    public void onDisable()
    {
        getLogger().info("Goodbye world!");
        // SaveArenas();
    }

    public void SaveArenas()
    {
        for (ArenaBlock ab : currentArenaBlocks)
        {
            String ArenaString = "";
            ArenaString = ArenaString + ab.arenaName + queryStringSep;
            ArenaString = ArenaString + LocationToString(ab.blockToActivate)
                    + queryStringSep;
            ArenaString = ArenaString + LocationToString(ab.arenaWarp)
                    + queryStringSep;
            ArenaString = ArenaString
                    + LocationToString(ab.arenaBlockStartLocation)
                    + queryStringSep;
            ArenaString = ArenaString
                    + LocationToString(ab.LocationToCheckForPlayers)
                    + queryStringToNumberSep;
            ArenaString = ArenaString + ab.radius + queryNumberToStringSep;

            // ArenaString = ArenaString + ab.material.name()
            // + queryStringToNumberSep;

            ArenaString = ArenaString + ab.timerDelay + queryNumberSep;
            ArenaString = ArenaString + ab.timerPeriod + queryNumberSep;
            ArenaString = ArenaString + ab.maxRunTimes + queryNumberToStringSep;
            ArenaString = ArenaString
                    + ab.arenaBlockStartLocation.getWorld().getName();
            ArenaString = ArenaString + "'";
            //
            // try
            // {
            // String Insert = "EXEC InsertArena ";
            // String Update = "EXEC UpdateArena ";
            //
            // ResultSet arenaExists = sql.query("EXEC ArenaExists '"
            // + ab.arenaName + "'");
            //
            // arenaExists.first();
            //
            // if (arenaExists.getString(1) == "0")
            // {
            // PreparedStatement ps = sql.prepare(Insert + ArenaString);
            // ps.execute();
            // }
            // else
            // {
            // PreparedStatement ps = sql.prepare(Update + ArenaString);
            // ps.execute();
            // }
            // }
            // catch (SQLException e)
            // {
            // e.printStackTrace();
            // }
        }
    }

    private void RecreateArenas()
    {

    }

    // public void CreateArena(List<String> currentValues)
    // {
    // World world = this.getServer().getWorld(currentValues.get(10));
    // Block BlockToActivate = StringToLocation(currentValues.get(1), world)
    // .getBlock();
    //
    // BlockToActivate.setMetadata("ArenaBlock", new FixedMetadataValue(this,
    // "Something"));
    //
    // ArenaBlock newArenaBlock = new ArenaBlock(BlockToActivate,
    // currentValues.get(0), Integer.valueOf(currentValues.get(5)),
    // MatToMob(currentValues.get(0)), this,
    // Integer.valueOf(currentValues.get(7)),
    // Integer.valueOf(currentValues.get(8)));
    //
    // newArenaBlock.arenaWarp = StringToLocation(currentValues.get(2), world)
    // .getBlock();
    // newArenaBlock.maxRunTimes = Integer.valueOf(currentValues.get(9));
    // currentArenaBlocks.add(newArenaBlock);
    // }

    public Location StringToLocation(String s, World world)
    {
        String[] strings = s.split(",");
        int x = Integer.valueOf(strings[0].trim());
        int y = Integer.valueOf(strings[1].trim());
        int z = Integer.valueOf(strings[2].trim());

        return new Location(world, x, y, z);
    }

    public String LocationToString(Block block)
    {
        String locationString = "";
        Location l = block.getLocation();

        locationString = String.valueOf(l.getBlockX()) + ","
                + String.valueOf(l.getBlockY()) + ","
                + String.valueOf(l.getBlockZ());
        return locationString;
    }

    public String LocationToString(Location l)
    {
        String locationString = "";

        locationString = String.valueOf(l.getBlockX()) + ","
                + String.valueOf(l.getBlockY()) + ","
                + String.valueOf(l.getBlockZ());
        return locationString;
    }

}
