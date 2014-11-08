package belven.arena;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
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
import resources.Group;
import belven.arena.arenas.BaseArena;
import belven.arena.arenas.BaseArena.ArenaTypes;
import belven.arena.arenas.PvPArena;
import belven.arena.arenas.StandardArena;
import belven.arena.arenas.TempArena;
import belven.arena.challengeclasses.ChallengeBlock;
import belven.arena.listeners.ArenaListener;
import belven.arena.listeners.BlockListener;
import belven.arena.listeners.MobListener;
import belven.arena.listeners.PlayerListener;
import belven.arena.rewardclasses.Item.ChanceLevel;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class ArenaManager extends JavaPlugin {
	private final PlayerListener newplayerListener = new PlayerListener(this);
	private final BlockListener blockListener = new BlockListener(this);
	private final ArenaListener arenaListener = new ArenaListener(this);
	private final MobListener mobListener = new MobListener(this);

	public static HashMap<String, String> commandPerms = new HashMap<String, String>();
	public static List<String> arenaPaths = new ArrayList<String>();
	public static HashMap<Integer, Gear> scalingGear = new HashMap<Integer, Gear>();
	private static HashMap<Material, ChanceLevel> itemChances = new HashMap<Material, ChanceLevel>();

	public List<BaseArena> currentArenaBlocks = new ArrayList<BaseArena>();
	public HashMap<Player, BaseArena> SelectedArenaBlocks = new HashMap<Player, BaseArena>();
	public HashMap<Player, BaseArena> PlayersInArenas = new HashMap<Player, BaseArena>();
	public List<Player> onlinePlayers = new ArrayList<Player>();

	public HashMap<String, Location> warpLocations = new HashMap<String, Location>();

	public List<ChallengeBlock> challengeBlocks = new ArrayList<ChallengeBlock>();

	private static String preFix = "BelvensArenas.";
	// public TeamManager teams = (TeamManager)
	// Bukkit.getServer().getPluginManager().getPlugin("BelvensTeams");

	static {
		// Item Chances
		ChanceLevel cl;
		// Always = 0 Players
		cl = ChanceLevel.Always;

		// VeryEasy = 1 Players
		cl = ChanceLevel.VeryEasy;
		itemChances.put(Material.COAL, cl);
		itemChances.put(Material.LOG, cl);
		itemChances.put(Material.WOOL, cl);
		itemChances.put(Material.SEEDS, cl);

		// Easy = 2 Players
		cl = ChanceLevel.Easy;
		itemChances.put(Material.BREAD, cl);
		itemChances.put(Material.IRON_INGOT, cl);
		itemChances.put(Material.GOLD_INGOT, cl);

		// Medium = 3 Players
		cl = ChanceLevel.Medium;
		itemChances.put(Material.REDSTONE, cl);
		itemChances.put(Material.EMERALD, cl);
		itemChances.put(Material.MELON_SEEDS, cl);

		// Hard = 4 Players
		cl = ChanceLevel.Hard;
		itemChances.put(Material.SADDLE, cl);
		itemChances.put(Material.EMERALD, cl);
		itemChances.put(Material.ENDER_PEARL, cl);
		itemChances.put(Material.NETHER_WARTS, cl);

		// VeryHard = 5 Players
		cl = ChanceLevel.VeryHard;
		itemChances.put(Material.DIAMOND, cl);
	}

	static {
		int i = 1;
		List<String> types = Arrays.asList("LEATHER", "GOLD", "IRON", "DIAMOND");

		String helmT = "_HELMET";
		String chestT = "_CHESTPLATE";
		String leggsT = "_LEGGINGS";
		String bootsT = "_BOOTS";
		String swordT = "_SWORD";

		for (String t : types) {
			ItemStack helm = new ItemStack(Material.getMaterial(t + helmT));
			ItemStack chest = new ItemStack(Material.getMaterial(t + chestT));
			ItemStack legs = new ItemStack(Material.getMaterial(t + leggsT));
			ItemStack boots = new ItemStack(Material.getMaterial(t + bootsT));
			ItemStack weapon;

			if (t == "LEATHER") {
				weapon = new ItemStack(Material.getMaterial("WOOD" + swordT));
			} else {
				weapon = new ItemStack(Material.getMaterial(t + swordT));
			}
			scalingGear.put(i, new Gear(helm, chest, legs, boots, weapon));
			i++;
		}

		commandPerms.put("s", preFix + "select");
		commandPerms.put("select", preFix + "select");
		commandPerms.put("createarena", preFix + "create");

		arenaPaths.add(0, ".Radius");
		arenaPaths.add(1, ".Timer Period");
		arenaPaths.add(2, ".Max Run Times");
		arenaPaths.add(3, ".Players Check Location");
		arenaPaths.add(4, ".World");
		arenaPaths.add(5, ".Activate Block");
		arenaPaths.add(6, ".Deactivate Block");
		arenaPaths.add(7, ".Arena Warp");
		arenaPaths.add(8, ".Spawn Start Location");
		arenaPaths.add(9, ".Spawn End Location");
		arenaPaths.add(10, ".Linked Arena Delay");
		arenaPaths.add(11, ".Elite Wave");
		arenaPaths.add(12, ".Start Location");
		arenaPaths.add(13, ".End Location");
		arenaPaths.add(14, ".Type");
		arenaPaths.add(15, ".Materials");
	}

	public static HashMap<Material, ChanceLevel> getItemChances() {
		return itemChances;
	}

	public static ArrayList<Material> getItemMaterials() {
		ArrayList<Material> mats = new ArrayList<Material>();
		mats.addAll(itemChances.keySet());
		return mats;
	}

	public static ChanceLevel getMaterialChance(Material m) {
		if (itemChances.containsKey(m)) {
			return itemChances.get(m);
		} else {
			return ChanceLevel.Easy;
		}
	}

	private ItemStack AddItemEnchantments(ItemStack is, String Path) {
		Set<String> enchants = getConfig().getConfigurationSection(Path + ".Enchantments").getKeys(false);

		for (String et : enchants) {
			String enchantPath = Path + ".Enchantments." + et;
			int level = getConfig().getInt(enchantPath);
			Enchantment e = Enchantment.getByName(et);
			is.addEnchantment(e, level);
		}
		return is;
	}

	private void AddLinkedArena(Player player, String arenaToAdd) {
		if (HasArenaBlockSelected(player)) {
			BaseArena selectedAB = GetSelectedArenaBlock(player);
			BaseArena arenaToLink = getArenaBlock(arenaToAdd);

			if (arenaToLink != null) {
				if (arenaToLink != selectedAB) {
					selectedAB.getLinkedArenas().add(arenaToLink);
					player.sendMessage(arenaToAdd + " was added to " + selectedAB.getName());
				} else {
					player.sendMessage("Arenas can't link to themselves");
				}
			} else {
				player.sendMessage("Can't find arena " + arenaToAdd);
			}
		}
	}

	public void setPlayerMetaData(BaseArena ba) {
		for (Player p : ba.getArenaPlayers()) {
			p.setMetadata("InArena",
					new FixedMetadataValue(this, new Group(ba.getArenaPlayers(), ba.getName(),
							ba.getType() == ArenaTypes.PvP)));
		}
	}

	@SuppressWarnings("deprecation")
	private ItemStack AddPotionFromConfig(String pe, String itemPath) {
		PotionEffectType pet = PotionEffectType.getByName(pe);

		if (pet == null) {
			return null;
		}

		PotionType pt = PotionType.getByEffect(pet);

		if (pt != null) {
			String potionPath = itemPath + ".Potions." + pe;
			int Amplifier = getConfig().getInt(potionPath + ".Amplifier");
			boolean Splash = getConfig().getBoolean(potionPath + ".Splash");
			Potion p = new Potion(pt, Amplifier == 0 ? 1 : Amplifier, Splash);
			return p.toItemStack(1);
		}
		return null;
	}

	private String ArenaPath(String arenaName) {
		return "Arenas." + arenaName;
	}

	private String ArenaRewardsPath(String arenaName) {
		return ArenaPath(arenaName) + ".Rewards";
	}

	private void ClearArena(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			ab.Deactivate();
			player.sendMessage("Arena " + ab.ArenaName() + " has been cleared");
		}
	}

	private void CreatePvPArena(Player p, Block block, String[] args) {
		block.setMetadata(MDM.ArenaBlock, new FixedMetadataValue(this, "Something"));

		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		Selection sel = worldEdit.getSelection(p);

		if (sel != null) {
			if (sel instanceof CuboidSelection) {
				Location min = sel.getMinimumPoint();
				Location max = sel.getMaximumPoint();
				String ArenaName = args[1];
				int Radius = Integer.valueOf(args[2]);
				Material m = Material.valueOf(args[3]);

				PvPArena newArenaBlock = new PvPArena(min, max, ArenaName, Radius, this, m,
						Functions.SecondsToTicks(Integer.valueOf(args[4])));

				SelectedArenaBlocks.put(p, newArenaBlock);
				currentArenaBlocks.add(newArenaBlock);
				p.sendMessage("Arena " + newArenaBlock.getName() + " was created");
			}
		} else {
			p.sendMessage("Use world edit to select the region");
		}
	}

	private void CreateStandardArena(Player p, Block block, String[] args) {
		block.setMetadata(MDM.ArenaBlock, new FixedMetadataValue(this, "Something"));

		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		Selection sel = worldEdit.getSelection(p);

		if (sel != null) {
			if (sel instanceof CuboidSelection) {
				Location min = sel.getMinimumPoint();
				Location max = sel.getMaximumPoint();

				String ArenaName = args[1];
				int Radius = Integer.valueOf(args[2]);
				MobToMaterialCollecton mobs = MatToMob(Material.getMaterial(args[3]));

				StandardArena newArenaBlock = new StandardArena(min, max, ArenaName, Radius, mobs, this,
						Functions.SecondsToTicks(Integer.valueOf(args[4])));

				SelectedArenaBlocks.put(p, newArenaBlock);
				currentArenaBlocks.add(newArenaBlock);
				p.sendMessage("Arena " + newArenaBlock.getName() + " was created");
			}
		} else {
			p.sendMessage("Use world edit to select the region");
		}
	}

	private void CreateTempArena(Player p) {
		if (!IsPlayerInArena(p)) {
			int maxSize = 0;
			int Radius = 40;
			int period = 0;

			Location pLoc = p.getLocation();

			Player[] tempPlayers = EntityFunctions.getNearbyPlayersNew(pLoc, Radius - 2 + Radius / 2);

			Radius = 0;

			for (Player pl : tempPlayers) {
				if (!IsPlayerInArena(pl)) {
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

			MobToMaterialCollecton mobs = MatToMob(Functions.offsetLocation(p.getLocation(), 0, -1, 0).getBlock()
					.getType());

			new TempArena(min, max, ArenaName, Radius, mobs, this, Functions.SecondsToTicks(period));
		} else {
			p.sendMessage("You can't do this while in an arena!!");
		}
	}

	private boolean EditArenaCommand(Player player, String[] args) {
		switch (args[0]) {
		case "savearena":
		case "sa":
			SaveArena(player);
			return true;

		case "setwaves":
		case "sw":
			SetWaves(player, args[1]);
			return true;

		case "setarenaspawnarea":
		case "sasa":
			SetArenaSpawnArea(player);
			return true;

		case "setarenaarea":
		case "saa":
			SetArenaArea(player);
			return true;

		case "setarenarewards":
		case "sar":
			SetArenaRewards(player);
			return true;

		case "setwavetimer":
		case "swt":
			SetWaveTimer(player, args[1]);
			return true;

		case "setboss":
		case "sb":
			SetBoss(player, args[1]);
			return true;

		case "remove":
		case "r":
			RemoveArenaBlock(player);
			return true;

		case "setwarpblock":
		case "swb":
			SetWarpBlock(player);
			return true;

		case "setmobtomat":
		case "smtm":
			SetMobToMat(player, args[1], args[2]);
			return true;

		case "removemobtomat":
		case "rmtm":
			RemoveMobToMat(player, args[1], args[2]);
			return true;

		case "setelitewave":
		case "sew":
			SetEliteWave(player, args[1]);
			return true;

		case "setelitemob":
		case "sem":
			SetEliteMob(player, args[1]);
			return true;

		case "removeelitemob":
		case "rem":
			RemoveEliteMob(player, args[1]);
			return true;

		case "setdeactivateblock":
		case "sdab":
			SetDeactivateBlock(player);
			return true;

		case "setactivateblock":
		case "sab":
			MoveArenaBlock(player);
			return true;

		case "setlinkedarenadelay":
		case "slad":
			SetLinkedArenaDelay(player, args[1]);
			return true;

		case "addlinkedarena":
		case "ala":
			AddLinkedArena(player, args[1]);
			return true;

		case "removelinkedarena":
		case "rla":
			RemoveLinkedArena(player, args[1]);
			return true;

		case "select":
		case "s":
			SelectArena(player, args[1]);
			return true;

		case "createarena":
			if (args.length >= 4) {
				CreateStandardArena(player, player.getLocation().getBlock(), args);
				return true;
			}

		case "createpvparena":
			if (args.length >= 4) {
				CreatePvPArena(player, player.getLocation().getBlock(), args);
				return true;
			}
		}
		return false;
	}

	private void ForceStartArena(Player p) {
		if (HasArenaBlockSelected(p)) {
			GetSelectedArenaBlock(p).Activate();
		}
	}

	public BaseArena getArena(Player p) {
		return PlayersInArenas.get(p);
	}

	public BaseArena getArenaBlock(String arenaToSelect) {
		BaseArena tempArenaBlock = null;
		for (BaseArena ab : currentArenaBlocks) {
			if (ab.getName().equalsIgnoreCase(arenaToSelect)) {
				tempArenaBlock = ab;
				break;
			}
		}
		return tempArenaBlock;
	}

	private List<BaseArena> GetArenaLinkedArenas(String arenaName) {
		List<BaseArena> tempArenas = new ArrayList<BaseArena>();

		String path = "Arenas." + arenaName + ".Linked Arenas";

		if (!getConfig().contains(path)) {
			return tempArenas;
		}

		for (String lab : getConfig().getString(path).split(", ")) {
			BaseArena ab = getArenaBlock(lab);
			if (ab != null) {
				tempArenas.add(ab);
			} else {
				ReloadArenaFromConfig(lab);
				ab = getArenaBlock(lab);
				if (ab != null) {
					tempArenas.add(ab);
				}
			}
		}
		return tempArenas;
	}

	private MobToMaterialCollecton GetArenaMobs(String ArenaName, String Path) {
		MobToMaterialCollecton tempMobs = new MobToMaterialCollecton();
		Set<String> materials = getConfig().getConfigurationSection(Path + ".Mobs").getKeys(false);

		for (String mat : materials) {
			String[] mobs = getConfig().getString(Path + ".Mobs." + mat).split(", ");

			for (String et : mobs) {
				tempMobs.Add(EntityType.valueOf(et), Material.valueOf(mat));
			}
		}

		return tempMobs;
	}

	private List<ItemStack> GetArenaRewards(String ArenaName, String Path) {
		if (getConfig().getConfigurationSection(ArenaRewardsPath(ArenaName)) == null) {
			return new ArrayList<ItemStack>();
		}
		return GetItemsAtPath(ArenaRewardsPath(ArenaName));
	}

	private ItemStack GetItemFromPath(String item, String Path) {
		ItemStack currentItem;
		String itemPath = Path + item;
		Material mat = Material.getMaterial(item);

		if (mat != null) {
			int amount = getConfig().getInt(itemPath + ".Amount");
			int durability = getConfig().getInt(itemPath + ".Durability");
			currentItem = new ItemStack(mat, amount > 0 ? amount : 1);
			currentItem.setDurability((short) durability);

			ConfigurationSection enchantsConfig = getConfig().getConfigurationSection(itemPath + ".Enchantments");

			if (enchantsConfig != null) {
				currentItem = AddItemEnchantments(currentItem, itemPath);
			}
			return currentItem;
		}
		return new ItemStack(Material.AIR);
	}

	private List<ItemStack> GetItemsAtPath(String Path) {
		List<ItemStack> tempItems = new ArrayList<ItemStack>();

		Set<String> items = getConfig().getConfigurationSection(Path).getKeys(false);

		for (String item : items) {
			if (item.equals("Potions")) {
				ConfigurationSection potionsConfig = getConfig().getConfigurationSection(Path + ".Potions");
				if (potionsConfig != null) {
					for (String pe : potionsConfig.getKeys(false)) {
						tempItems.add(AddPotionFromConfig(pe, Path));
					}
				}
			} else {
				tempItems.add(GetItemFromPath(item, Path));
			}
		}
		return tempItems;
	}

	public BaseArena GetSelectedArenaBlock(Player p) {
		return SelectedArenaBlocks.get(p);
	}

	@SuppressWarnings("deprecation")
	private void GiveArenaRewards(Player p) {
		if (HasArenaBlockSelected(p)) {
			BaseArena ab = GetSelectedArenaBlock(p);
			ab.getArenaPlayers().add(p);
			ab.GiveRewards();
			ab.getArenaPlayers().remove(p);
			p.updateInventory();
			p.sendMessage("You were given arena " + ab.ArenaName() + "s rewards");
		}
	}

	private boolean HasArenaBlockSelected(Player player) {
		if (GetSelectedArenaBlock(player) == null) {
			player.sendMessage("Please select an Arena using /ba select <ArenaName>");
			return false;
		} else {
			return true;
		}
	}

	public boolean IsPlayerInArena(Player p) {
		return PlayersInArenas.containsKey(p);
	}

	public void ItemStackToPath(ItemStack is, String Path) {
		if (is == null) {
			return;
		}

		if (is.getType() == Material.POTION) {
			SaveItemPotionEffect(is, Path);
		} else {
			Path += "." + is.getType().toString();
			getConfig().set(Path + ".Amount", is.getAmount());
			getConfig().set(Path + ".Durability", is.getDurability());

			if (is.getEnchantments().size() > 0) {
				SaveItemEnchantments(is, Path);
			}
		}
	}

	public void LeaveArena(Player p) {
		if (IsPlayerInArena(p)) {
			BaseArena ab = getArena(p);
			ab.getArenaPlayers().remove(p);
			PlayersInArenas.remove(p);
			setPlayerMetaData(ab);

			if (ab.getArenaPlayers().size() == 0) {
				ab.Deactivate();
			}

			if (warpLocations.get(p) != null) {
				p.teleport(warpLocations.get(p));
				warpLocations.put(p.getName(), null);
				p.sendMessage("You left the arena " + ab.ArenaName() + " and have been returned to your last warp");
				return;
			}
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			p.sendMessage("You left the arena " + ab.ArenaName());
		}
	}

	private boolean ListArenaCommands(Player player, String[] args) {
		switch (args[0]) {
		case "listarenas":
		case "la":
			ListArenas(player);
			return true;

		case "listmobs":
		case "lm":
			ListMobs(player);
			return true;

		case "listlinkedarenas":
		case "lla":
			ListLinkedArenas(player);
			return true;
		}
		return false;
	}

	private void ListArenas(Player player) {
		for (BaseArena ab : currentArenaBlocks) {
			player.sendMessage(ab.getName());
		}
	}

	private void ListLinkedArenas(Player p) {
		if (HasArenaBlockSelected(p)) {
			for (BaseArena lab : GetSelectedArenaBlock(p).getLinkedArenas()) {
				p.sendMessage(lab.getName());
			}
		}
	}

	private void ListMobs(Player p) {
		if (HasArenaBlockSelected(p)) {
			BaseArena ab = GetSelectedArenaBlock(p);

			if (ab != null) {
				if (ab.getType() != ArenaTypes.PvP) {
					StandardArena sab = (StandardArena) ab;
					for (MobToMaterial mtm : sab.getMobToMat().MobToMaterials) {
						p.sendMessage(mtm.et.name() + "," + mtm.m.name());
					}
				}
			}
		}
	}

	private String LocationToString(Block block) {
		String locationString = "";
		Location l = block.getLocation();

		locationString = String.valueOf(l.getBlockX()) + "," + String.valueOf(l.getBlockY()) + ","
				+ String.valueOf(l.getBlockZ());
		return locationString;
	}

	private String LocationToString(Location l) {
		String locationString = "";

		if (l != null) {
			locationString = String.valueOf(l.getBlockX()) + "," + String.valueOf(l.getBlockY()) + ","
					+ String.valueOf(l.getBlockZ());
		}
		return locationString;
	}

	private MobToMaterialCollecton MatToMob(Material mat) {
		MobToMaterialCollecton spawnMats = new MobToMaterialCollecton();
		spawnMats.Add(EntityType.ZOMBIE, mat);
		spawnMats.Add(EntityType.SKELETON, mat);
		return spawnMats;
	}

	private void MoveArenaBlock(Player p) {
		if (HasArenaBlockSelected(p)) {
			Block tempBlock = GetSelectedArenaBlock(p).getBlockToActivate();
			tempBlock.removeMetadata("ArenaBlock", this);
			tempBlock = p.getLocation().getBlock();
			tempBlock.setType(Material.REDSTONE_WIRE);

			tempBlock.setMetadata(MDM.ArenaBlock, new FixedMetadataValue(this, "Something"));

			GetSelectedArenaBlock(p).setBlockToActivate(tempBlock);
			p.sendMessage(GetSelectedArenaBlock(p).getName() + " active block has moved!");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		String commandSent = cmd.getName();

		if (args.length <= 0) {
			return false;
		}

		if (commandPerms.containsKey(args[0])) {
			if (!player.hasPermission(commandPerms.get(args[0]))) {
				player.sendMessage("You need the permission " + commandPerms.get(args[0]) + " in order to do /"
						+ commandSent + args[0]);
				return false;
			}
		}

		return commandSent.equals("ba")
				&& (EditArenaCommand(player, args) || ListArenaCommands(player, args) || UtilityArenaCommands(player,
						args));
	}

	@Override
	public void onDisable() {
		SaveArenas();
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(newplayerListener, this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(arenaListener, this);
		pm.registerEvents(mobListener, this);
		RecreateArenasFromConfig();
	}

	private void RecreateArenasFromConfig() {
		String currentPath = "Arenas.";
		ConfigurationSection tempArenas = getConfig().getConfigurationSection(currentPath);

		if (tempArenas != null) {
			Set<String> arenas = tempArenas.getKeys(false);

			for (String ArenaName : arenas) {
				ReloadArenaFromConfig(ArenaName);
			}
		}
	}

	private void ReloadArena(Player p) {
		if (HasArenaBlockSelected(p)) {
			BaseArena ab = GetSelectedArenaBlock(p);
			ReloadArenaFromConfig(ab.getName());
			p.sendMessage("Arena " + ab.ArenaName() + " has been reloaded");
		}
	}

	private void ReloadArenaFromConfig(String ArenaName) {
		if (getArenaBlock(ArenaName) != null) {
			return;
		}

		String currentPath = "Arenas.";
		String path = currentPath + ArenaName;
		FileConfiguration con = getConfig();

		int radius = con.getInt(path + arenaPaths.get(0));
		int timerPeriod = con.getInt(path + arenaPaths.get(1));
		int maxRunTimes = con.getInt(path + arenaPaths.get(2));
		int linkedArenaDelay = con.getInt(path + arenaPaths.get(10));

		String worldName = con.getString(path + arenaPaths.get(4));

		if (worldName == null) {
			worldName = "world";
		}

		World world = this.getServer().getWorld(worldName);

		if (world == null) {
			WorldCreator wc = new WorldCreator(worldName);
			world = this.getServer().createWorld(wc);

			if (world == null) {
				return;
			}
		}

		Block blockToActivate = StringToLocation(con.getString(path + arenaPaths.get(5)), world).getBlock();

		Block deactivateBlock = StringToLocation(con.getString(path + arenaPaths.get(6)), world).getBlock();

		Block arenaWarp = StringToLocation(con.getString(path + arenaPaths.get(7)), world).getBlock();

		Location spawnAreaStartLocation = StringToLocation(con.getString(path + arenaPaths.get(8)), world);

		Location spawnAreaEndLocation = StringToLocation(con.getString(path + arenaPaths.get(9)), world);

		Location AreaStartLocation = StringToLocation(con.getString(path + arenaPaths.get(12)), world);

		Location AreaEndLocation = StringToLocation(con.getString(path + arenaPaths.get(13)), world);

		BaseArena ab = null;

		if (con.getString(path + arenaPaths.get(14)).equalsIgnoreCase("standard")) {
			MobToMaterialCollecton mobs = GetArenaMobs(ArenaName, path);
			int eliteWave = con.getInt(path + arenaPaths.get(11));

			ab = new StandardArena(spawnAreaStartLocation, spawnAreaEndLocation, ArenaName, radius, mobs, this,
					timerPeriod);
			ab.setEliteWave(eliteWave);
		} else if (con.getString(path + arenaPaths.get(14)).equalsIgnoreCase("pvp")) {
			// TODO
			Material m = Material.GRASS;
			if (con.contains(path + arenaPaths.get(15))) {
				m = Material.getMaterial(con.getString(path + arenaPaths.get(15)));
			}

			ab = new PvPArena(spawnAreaStartLocation, spawnAreaEndLocation, ArenaName, radius, this, m, timerPeriod);
		}

		blockToActivate.setMetadata(MDM.ArenaBlock, new FixedMetadataValue(this, "Something"));

		if (getArenaBlock(ArenaName) != null) {
			ab = getArenaBlock(ArenaName);
			ab.setArenaWarp(arenaWarp);
			ab.setArenaStartLocation(AreaStartLocation);
			ab.setArenaEndLocation(AreaEndLocation);
			ab.setArenaRewards(GetArenaRewards(ArenaName, path));
			ab.setLinkedArenas(GetArenaLinkedArenas(ArenaName));
			ab.setBlockToActivate(blockToActivate);
			ab.setDeactivateBlock(deactivateBlock);
			ab.setLinkedArenaDelay(linkedArenaDelay);
			ab.setMaxRunTimes(maxRunTimes <= 0 ? 1 : maxRunTimes);
			getLogger().info(ArenaName + " has been created");
		}
	}

	private void RemoveArenaBlock(Player p) {
		if (HasArenaBlockSelected(p)) {
			String arenaName = GetSelectedArenaBlock(p).getName();
			Block ab = GetSelectedArenaBlock(p).getBlockToActivate();
			ab.removeMetadata("ArenaBlock", this);
			String path = "Arenas." + GetSelectedArenaBlock(p).getName();
			getConfig().set(path, null);
			currentArenaBlocks.remove(GetSelectedArenaBlock(p));
			SelectedArenaBlocks.remove(GetSelectedArenaBlock(p));
			p.sendMessage(arenaName + " was removed");
		}
	}

	private void RemoveEliteMob(Player player, String et) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);

			if (ab.getType() != ArenaTypes.PvP) {
				player.sendMessage(((StandardArena) ab).getEliteMobCollection().Remove(EntityType.valueOf(et)));
			}
		}
	}

	private void RemoveLinkedArena(Player player, String arenaToRemove) {
		if (HasArenaBlockSelected(player)) {
			BaseArena sab = GetSelectedArenaBlock(player);

			if (sab != null) {
				BaseArena ab = getArenaBlock(arenaToRemove);
				if (ab != null) {
					ab.getLinkedArenas().remove(getArenaBlock(arenaToRemove));
					player.sendMessage(arenaToRemove + " was removed from " + ab.ArenaName());
				} else {
					player.sendMessage("Can't find arena " + arenaToRemove);

				}
			}
		}
	}

	private void RemoveMobToMat(Player player, String et, String m) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			if (ab.getType() != ArenaTypes.PvP) {
				StandardArena sab = (StandardArena) ab;
				player.sendMessage(sab.getMobToMat().Remove(et, m));
			}
		}
	}

	private void SaveArena(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			SaveArenaToConfig(ab);
			player.sendMessage("Arena " + ab.ArenaName() + " was saved");
		}
	}

	private void SaveArenaEliteMobs(StandardArena ab) {
		String path = ArenaPath(ab.getName()) + ".EliteMobs.";

		for (EliteMob em : ab.getEliteMobCollection().ems) {
			String typePath = path + em.type.name();
			Gear g = em.armor;

			if (g.h != null) {
				ItemStackToPath(g.h, typePath + ".Helmet");
			}

			if (g.c != null) {
				ItemStackToPath(g.c, typePath + ".ChestPlate");
			}

			if (g.l != null) {
				ItemStackToPath(g.l, typePath + ".Leggins");
			}

			if (g.b != null) {
				ItemStackToPath(g.b, typePath + ".Boots");
			}

			if (g.w != null) {
				ItemStackToPath(g.w, typePath + ".Weapon");
			}
		}
	}

	private void SaveArenaMobs(StandardArena ab) {
		String path = ArenaPath(ab.getName());

		for (Material m : ab.getMobToMat().Materials()) {
			String entities = "";

			for (EntityType et : ab.getMobToMat().EntityTypes(m)) {
				entities += et.toString() + ", ";
			}

			entities = entities.substring(0, entities.length() - 2);
			getConfig().set(path + ".Mobs." + m.toString(), entities);
		}
	}

	private void SaveArenaRewards(BaseArena ab) {
		String path = ArenaRewardsPath(ab.getName());

		for (ItemStack is : ab.getArenaRewards()) {
			ItemStackToPath(is, path);
		}
	}

	private void SaveArenas() {
		for (BaseArena ab : currentArenaBlocks) {
			SaveArenaToConfig(ab);
		}
	}

	private void SaveArenaToConfig(BaseArena ab) {
		String path = ArenaPath(ab.getName());
		getServer().getLogger().info(ab.getName() + " was saved");
		getConfig().set(path, null);
		getConfig().set(path + arenaPaths.get(1), ab.getTimerPeriod());
		getConfig().set(path + arenaPaths.get(2), ab.getMaxRunTimes());

		getConfig().set(path + arenaPaths.get(4), ab.getSpawnArenaStartLocation().getWorld().getName());

		getConfig().set(path + arenaPaths.get(5), LocationToString(ab.getBlockToActivate()));

		getConfig().set(path + arenaPaths.get(6), LocationToString(ab.getDeactivateBlock()));

		getConfig().set(path + arenaPaths.get(7), LocationToString(ab.getArenaWarp()));

		getConfig().set(path + arenaPaths.get(8), LocationToString(ab.getSpawnArenaStartLocation()));

		getConfig().set(path + arenaPaths.get(9), LocationToString(ab.getSpawnArenaEndLocation()));

		getConfig().set(path + arenaPaths.get(10), ab.getLinkedArenaDelay());

		getConfig().set(path + arenaPaths.get(12), LocationToString(ab.getArenaStartLocation()));

		getConfig().set(path + arenaPaths.get(13), LocationToString(ab.getArenaEndLocation()));

		getConfig().set(path + arenaPaths.get(14), ab.getType().name());

		SaveLinkedArenas(ab);
		SaveArenaRewards(ab);

		if (ab.getType() != ArenaTypes.PvP) {
			StandardArena sab = (StandardArena) ab;
			SaveArenaEliteMobs(sab);
			getConfig().set(path + arenaPaths.get(11), ab.getEliteWave());
			SaveArenaMobs(sab);
			getConfig().set(path + ".Boss.Type", sab.getBossMob().BossType.toString());
		}
		saveConfig();
	}

	private void SaveItemEnchantments(ItemStack is, String currentPath) {
		Map<Enchantment, Integer> enchants = is.getEnchantments();

		if (!enchants.isEmpty()) {
			currentPath += ".Enchantments.";

			for (Enchantment e : enchants.keySet()) {
				getConfig().set(currentPath + e.getName(), enchants.get(e));
			}
		}
	}

	private void SaveItemPotionEffect(ItemStack is, String currentPath) {
		Potion p = Potion.fromItemStack(is);
		currentPath += ".Potions.";
		Collection<PotionEffect> fx = p.getEffects();
		for (PotionEffect pe : fx) {
			String path = currentPath + pe.getType().getName();
			getConfig().set(path + ".Amplifier", pe.getAmplifier());
			getConfig().set(path + ".Splash", p.isSplash());
		}
	}

	private void SaveLinkedArenas(BaseArena ab) {
		if (ab.getLinkedArenas().size() > 0) {
			String path = "Arenas." + ab.ArenaName() + ".Linked Arenas";
			String sb = "";

			for (BaseArena lab : ab.getLinkedArenas()) {
				sb += lab.getName() + ", ";
			}

			getConfig().set(path, sb);
		}
	}

	private void SelectArena(Player p, String arenaToSelect) {
		BaseArena tempArenaBlock = getArenaBlock(arenaToSelect);
		if (tempArenaBlock != null) {
			SelectedArenaBlocks.put(p, tempArenaBlock);
			p.sendMessage(arenaToSelect + " is now selected");
		} else {
			p.sendMessage("Can't find arena " + arenaToSelect);
		}
	}

	private void SetArenaArea(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);

			WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
			Selection sel = worldEdit.getSelection(player);

			if (sel != null) {
				if (sel instanceof CuboidSelection) {
					Location min = sel.getMinimumPoint();
					Location max = sel.getMaximumPoint();
					ab.setArenaStartLocation(min);
					ab.setArenaEndLocation(max);
					player.sendMessage("Arena " + ab.ArenaName() + "s region has been updated!!");
				}
			}
		}
	}

	private void SetArenaRewards(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			ab.getArenaRewards().clear();

			for (ItemStack is : player.getInventory()) {
				if (is != null) {
					ab.getArenaRewards().add(is);
				}
			}

			player.sendMessage("Arena " + ab.ArenaName() + " rewards have been set to your invetory.");
		}
	}

	private void SetArenaSpawnArea(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);

			WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
			Selection sel = worldEdit.getSelection(player);

			if (sel != null) {
				if (sel instanceof CuboidSelection) {
					Location min = sel.getMinimumPoint();
					Location max = sel.getMaximumPoint();
					ab.setSpawnArenaStartLocation(min);
					ab.setSpawnArenaEndLocation(max);

					player.sendMessage("Arena " + ab.ArenaName() + "s spawn region has been updated!!");
				}
			}
		}
	}

	private void SetBoss(Player player, String bossType) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			if (ab.getType() != ArenaTypes.PvP) {
				StandardArena sab = (StandardArena) ab;
				sab.getBossMob().BossType = EntityType.valueOf(bossType);
				sab.getBossMob().gear = new Gear(player);
				player.sendMessage("Arena " + ab.ArenaName() + " boss is now " + bossType);
			}
		}
	}

	private void SetDeactivateBlock(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			ab.setDeactivateBlock(player.getLocation().getBlock());
			player.sendMessage("Arena " + ab.ArenaName() + " deactivate block has moved!!");
		}
	}

	private void SetEliteMob(Player player, String et) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			if (ab.getType() != ArenaTypes.PvP) {

				player.sendMessage(((StandardArena) ab).getEliteMobCollection().Set(EntityType.valueOf(et), player));
			}
		}
	}

	private void SetEliteWave(Player player, String ew) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			ab.setEliteWave(Integer.valueOf(ew));
			player.sendMessage("Arena " + ab.ArenaName() + " elite wave is now " + ew);
		}
	}

	private void SetLinkedArenaDelay(Player player, String delay) {
		if (HasArenaBlockSelected(player)) {
			BaseArena selectedAB = GetSelectedArenaBlock(player);
			selectedAB.setLinkedArenaDelay(Integer.valueOf(delay));
			player.sendMessage(selectedAB.getName() + "s Linked Arena Delay is now " + delay);
		}
	}

	private void SetMobToMat(Player player, String et, String m) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			if (ab.getType() != ArenaTypes.PvP) {
				StandardArena sab = (StandardArena) ab;
				player.sendMessage(sab.getMobToMat().Add(et, m));
			}
		}
	}

	private void SetWarpBlock(Player p) {
		if (HasArenaBlockSelected(p)) {
			GetSelectedArenaBlock(p).setArenaWarp(p.getLocation().getBlock());
			p.sendMessage(GetSelectedArenaBlock(p).getName() + " warp block set!!");
		}
	}

	private void SetWaves(Player p, String runtimes) {
		if (HasArenaBlockSelected(p)) {
			GetSelectedArenaBlock(p).setMaxRunTimes(Integer.valueOf(runtimes));
			p.sendMessage(GetSelectedArenaBlock(p).getName() + " waves set to " + runtimes);
		}
	}

	private void SetWaveTimer(Player p, String newPeriod) {
		int period = Functions.SecondsToTicks(Integer.valueOf(newPeriod));

		if (HasArenaBlockSelected(p)) {
			GetSelectedArenaBlock(p).setTimerPeriod(period);
			p.sendMessage(GetSelectedArenaBlock(p).getName() + " mobs now spawn every " + newPeriod);
		}
	}

	private Location StringToLocation(String s, World world) {
		Location tempLoc = null;
		if (s != null) {
			String[] strings = s.split(",");
			int x = Integer.valueOf(strings[0].trim());
			int y = Integer.valueOf(strings[1].trim());
			int z = Integer.valueOf(strings[2].trim());
			tempLoc = new Location(world, x, y, z);
		}
		return tempLoc != null ? tempLoc : new Location(world, 0, 0, 0);
	}

	private void TeleportArenaMobs(Player player) {
		if (HasArenaBlockSelected(player)) {
			BaseArena ab = GetSelectedArenaBlock(player);
			if (ab.getType() != ArenaTypes.PvP) {
				StandardArena sab = (StandardArena) ab;
				for (LivingEntity le : sab.getArenaEntities()) {
					le.teleport(ab.getArenaWarp().getLocation());
				}
			}

		}
	}

	private boolean UtilityArenaCommands(Player player, String[] args) {
		switch (args[0]) {
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

	public boolean WarpToArena(Player p, BaseArena ab) {

		Location tpL = Functions.offsetLocation(ab.getArenaWarp().getLocation(), 0.5, 0, 0.5);
		String tpMsg = "You were teleported and added to the arena " + ab.ArenaName();

		if (ab.isActive()) {

			PlayersInArenas.put(p, ab);
			setPlayerMetaData(ab);

			if (!ab.getArenaPlayers().contains(p)) {
				ab.getArenaPlayers().add(p);
			} else {
				ab.getArenaPlayers().remove(p);
				ab.getArenaPlayers().add(p);
			}

			Block b = p.getLocation().getBlock();
			if (!b.hasMetadata("ArenaAreaBlock")) {
				warpLocations.put(p.getName(), p.getLocation());
				p.teleport(tpL, TeleportCause.COMMAND);
			}

			p.sendMessage(tpMsg);
			return true;
		} else {
			warpLocations.put(p.getName(), p.getLocation());
			p.teleport(tpL, TeleportCause.COMMAND);
			p.sendMessage(tpMsg);
			return true;
		}
	}

	public void WarpToArena(Player player, String arenaToWarp) {
		BaseArena tempArenaBlock = getArenaBlock(arenaToWarp);
		if (tempArenaBlock != null) {
			WarpToArena(player, tempArenaBlock);
		}
	}
}