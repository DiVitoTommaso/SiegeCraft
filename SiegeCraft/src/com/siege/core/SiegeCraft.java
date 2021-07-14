package com.siege.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.siege.data.Robot;
import com.siege.data.SiegeBoard;
import com.siege.data.Tower;
import com.siege.data.constants.SiegeColor;
import com.siege.data.constants.SiegeItems;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import siege.exeptions.SiegeException;
import siege.listeners.SiegeBaseListener;
import siege.listeners.SiegeGameListener;
import siege.util.BukkitRestricted;
import siege.util.ImmutableLocation;
import javafx.collections.ObservableList;

/**
 * plugin class
 * 
 * @author Tommaso
 *
 */
@BukkitRestricted
public final class SiegeCraft extends JavaPlugin implements Listener {

	private static SiegeCraft instance;

	// red and blue team lists
	private final ObservableList<Player> redTeam = FXCollections.observableArrayList();
	private final ObservableList<Player> blueTeam = FXCollections.observableArrayList();

	// support game class
	private final SiegeBoard scoreboard = new SiegeBoard(redTeam, blueTeam);
	private final SiegeGame game = new SiegeGame(scoreboard);

	// list of player placed blocks
	private final HashSet<ImmutableLocation> placedBlocks = new HashSet<>();

	/**
	 * constructor for SiegeCraft plugin
	 * 
	 * @throws IllegalAccessError if plugin already has been instantiated
	 */
	public SiegeCraft() {
		checkThread();

		if (instance != null)
			throw new IllegalAccessError(
					"Detected 2 plugin instances. Please don't create any instance and use getInstance()");

		registerListener(SiegeBaseListener.class);
	}

	/**
	 * register new Listener by class name
	 * 
	 * @param s the class of the listener to be registered
	 * @throws SiegeException        if listener cannot be instantiated
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void registerListener(Class<? extends SiegeGameListener> s) {
		checkThread();

		try {
			getListeners().add(s.getConstructor(SiegeBoard.class, ObservableList.class, ObservableList.class)
					.newInstance(scoreboard, redTeam, blueTeam));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new SiegeException("Listener cannot be instantiated. Reason: " + e.getMessage());
		}
	}

	/**
	 * get all siege game listeners
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 * 
	 * @return the list of all game listeners editable (remove and add are allowed)
	 */
	public ObservableList<SiegeGameListener> getListeners() {
		checkThread();

		return game.getListeners();
	}

	/**
	 * set the spawn point for a team
	 * 
	 * @param l         a block where they must respawn
	 * @param teamColor a team color owner of this spawn
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setSpawn(Location l, SiegeColor teamColor) {
		checkThread();

		if (l == null || teamColor == null)
			throw new SiegeException("Null and negative values are not allowed");

		if (teamColor == SiegeColor.BLUE) {
			// set blue spawn
			game.setBlueSpawn(l);
			for (int x = -1; x <= 1; x++)
				for (int z = -1; z <= 1; z++) {
					game.getBlueSpawn().add(x, -1, z);
					game.getBlueSpawn().getBlock().setType(Material.BLUE_WOOL);
					game.getBlueSpawn().add(-x, +1, -z);
				}
		} else {
			// set red spawn
			game.setRedSpawn(l);
			for (int x = -1; x <= 1; x++)
				for (int z = -1; z <= 1; z++) {
					game.getRedSpawn().add(x, -1, z);
					game.getRedSpawn().getBlock().setType(Material.RED_WOOL);
					game.getRedSpawn().add(-x, +1, -z);
				}
		}

	}

	/**
	 * set the powerup spawn location
	 * 
	 * @param l       the middle point of the spawn area
	 * @param seconds delay between powerup spawn
	 * @param radius  the radius of the spawn area
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setPowerupSpawn(Location l, int seconds, int radius) {
		checkThread();

		createCircle(l, radius, Material.GREEN_CONCRETE);
		if (game.isPlaying())
			throw new SiegeException("Cannot change powerup spawn during game");

		if (l == null || seconds <= 0 || radius <= 0)
			throw new SiegeException("Null and negative values are not allowed");

		l.add(0, -1, 0);
		l.getBlock().setType(Material.EMERALD_BLOCK);
		l.add(0, 1, 0);
		// set spawn location and details
		game.setPowerupSpawn(l);
		game.getSettings().put("powerupSpawnDelay", seconds);
		game.getSettings().put("powerupSpawnRadius", radius);

	}

	/**
	 * spawn a new robot
	 * 
	 * @param l         spawn location
	 * @param ownerTeam owner of the robot
	 * @param damage    base damage
	 * @param health    robot health
	 * @return the spawned robot
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public Robot spawnRobot(Location l, SiegeColor ownerTeam, int damage, int health) {
		checkThread();

		if (!game.isPlaying())
			throw new SiegeException("Robots can be spawned only during game");

		if (l == null || ownerTeam == null || damage <= 0 || health <= 0)
			throw new SiegeException("Null and negative values are not allowed");
		Tower ally = ownerTeam == SiegeColor.BLUE ? game.getBlueTower() : game.getRedTower();
		Tower enemy = ownerTeam == SiegeColor.BLUE ? game.getRedTower() : game.getBlueTower();

		// create robot and call every listener every time robot gets damage
		Robot r = ally.createRobot(enemy, damage, health);
		r.onRobotDamage(() -> game.getListeners().forEach(e -> e.onRobotDamage(ownerTeam, r)));
		// call every listener on robot spawn
		game.getListeners().forEach(e -> e.onRobotSpawn(ownerTeam, r, 0));

		return r;
	}

	/**
	 * spawn a tower at the given location for a team based on the color with the
	 * specified damage per tick, reload speed, tower radius and health
	 * 
	 * Note: negative values will be considerated as positive using v)
	 * 
	 * @param l                    the tower position
	 * @param color                team color RED/BLUE only supported
	 * @param damage               damage per tick
	 * @param towerRadius          tower radius
	 * @param health               health of the tower.
	 * @param robotLevelMultiplier base robot stats multiplier per level
	 * @param robotBaseDamage      robot base damage
	 * @param robotHealth          robot health
	 * @return the created tower
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 * 
	 */
	public Tower spawnTower(Location l, SiegeColor color, int damage, int towerRadius, int health,
			double robotLevelMultiplier, int robotBaseDamage, int robotHealth) {
		checkThread();

		if (game.isPlaying())
			throw new SiegeException("Cannot change tower during game");

		if (l == null || color == null || damage <= 0 || robotLevelMultiplier <= 0 || towerRadius <= 0 || health <= 0
				|| robotHealth <= 0)
			throw new SiegeException("Null and negative values are not allowed");

		createCircle(l, towerRadius, color == SiegeColor.BLUE ? Material.BLUE_CONCRETE : Material.RED_CONCRETE);

		if (color == SiegeColor.BLUE) {
			// create tower and call every listener every time tower gets damage
			game.setBlueTower(new Tower(blueTeam, redTeam, l, damage, towerRadius, health, robotLevelMultiplier,
					robotBaseDamage, robotHealth));

			game.getBlueTower().onTowerDamage(() -> {
				if (game.getBlueTower().getHealth() <= 0)
					stop();
			});
			return game.getBlueTower();
		} else {
			// create tower and call every listener every time tower gets damage
			game.setRedTower(new Tower(redTeam, blueTeam, l, damage, towerRadius, health, robotLevelMultiplier,
					robotBaseDamage, robotHealth));

			game.getRedTower().onTowerDamage(() -> {
				if (game.getRedTower().getHealth() <= 0)
					stop();
			});
			return game.getRedTower();
		}
	}

	/**
	 * start a new game of siege if are set towers, player spawn areas, coins spawn
	 * area with the given max play time and robot spawn delay
	 * 
	 * @param maxPlayTime max time playable in seconds
	 * @param robotDelay  robot spawn check delay in seconds
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void startGame(int maxPlayTime, int robotDelay) {
		checkThread();

		game.checkState();

		game.getSettings().put("maxPlayTime", maxPlayTime);
		game.getSettings().put("robotSpawnDelay", robotDelay);

		game.play();

	}

	/**
	 * set the team for a list of players
	 * 
	 * @param team    a team color
	 * @param players a player's names list
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setPlayersTeam(SiegeColor team, List<String> players) {
		checkThread();

		if (game.isPlaying())
			throw new SiegeException("Cannot change players teams during game");

		if (team == null || players == null)
			throw new SiegeException("Null values are not allowed");

		for (String p : players) {
			// check if players are in team
			if (blueTeam.contains(Bukkit.getPlayer(p)) || redTeam.contains(Bukkit.getPlayer(p)))
				throw new SiegeException("Player already in a team");

			// add the player
			if (team == SiegeColor.BLUE)
				blueTeam.add(Bukkit.getPlayer(p));
			else
				redTeam.add(Bukkit.getPlayer(p));
		}
	}

	/**
	 * remove players from every team
	 * 
	 * @param players list of player to remove from their team
	 * @return the team from which the player was removed
	 * 
	 * @throws SiegeException        if an error occur
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public SiegeColor removePlayersTeam(List<String> players) {
		checkThread();

		if (game.isPlaying())
			throw new SiegeException("Cannot change players teams during game");

		for (String p : players) {
			if (blueTeam.remove(Bukkit.getPlayer(p)))
				return SiegeColor.BLUE;
			if (redTeam.remove(Bukkit.getPlayer(p)))
				return SiegeColor.RED;
		}

		return null;

	}

	/**
	 * ask to stop the game
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void stop() {
		checkThread();

		if (!game.isPlaying())
			return;

		redTeam.clear();
		blueTeam.clear();

		placedBlocks.forEach(e -> e.getBlock().setType(Material.AIR));
		placedBlocks.clear();

		game.expireTimer();
	}

	/**
	 * internal method to stop game when time expire
	 */

	private void timerStop() {
		redTeam.clear();
		blueTeam.clear();

		placedBlocks.forEach(e -> e.getBlock().setType(Material.AIR));
		placedBlocks.clear();
	}

	/**
	 * on eneable plugin method DON'T CALL!
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	@Override
	public void onEnable() {
		checkThread();

		if (instance == null)
			Bukkit.getPluginManager().registerEvents(this, this);

		instance = this;

		if (getDataFolder().exists())
			try {
				game.setRedTower(Tower.fromFile(redTeam, blueTeam, new File(getDataFolder(), "Red tower.sc")));
				game.setBlueTower(Tower.fromFile(blueTeam, redTeam, new File(getDataFolder(), "Blue tower.sc")));

				game.setRedSpawn(Location.deserialize(read("Red spawn.sc")));
				game.setBlueSpawn(Location.deserialize(read("Blue spawn.sc")));

				game.setPowerupSpawn(Location.deserialize(read("Powerup spawn.sc")));
				game.getSettings().putAll(read("Game settings.sc"));
			} catch (Exception e) {
				System.err.println("Siege files are corrupted. Enabling default settings...");
			}

		// add listener for time expired
		game.onTimeChange(e -> {
			if (e <= 0)
				timerStop();
			return null;
		});

		scoreboard.init();

		// add listener to check if player exists
		blueTeam.addListener(SiegeCraft::onPlayerAdd);
		redTeam.addListener(SiegeCraft::onPlayerAdd);

		getCommand("spawnrobot").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("Blue", "Red");
				return null;
			}
		});

		getCommand("spawnrobot").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (args.length < 1)
					return false;

				// check caller is player
				Player p = checkAndCast(sender);

				try {
					spawnRobot(p.getLocation(), SiegeColor.valueOf(args[0].toUpperCase()), 30, 2000);
					sender.sendMessage(SiegeColor.valueOf(args[0].toUpperCase()) + "" + ChatColor.GREEN
							+ " robot spawned successfully");
					return true;

				} catch (SiegeException e) {
					sender.sendMessage(ChatColor.RED + "Command error: " + e.getMessage());
					return false;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "Command error: invalid team color");
					return false;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					e.printStackTrace();
					return false;
				}
			}

		});

		getCommand("setspawn").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("Blue", "Red");
				return null;
			}
		});

		getCommand("setspawn").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (args.length < 1)
					return false;

				// check caller is player
				Player p = checkAndCast(sender);

				try {
					// try to set the player spawn
					setSpawn(p.getLocation(), SiegeColor.valueOf(args[0].toUpperCase()));
					sender.sendMessage(SiegeColor.valueOf(args[0].toUpperCase()) + "" + ChatColor.GREEN
							+ " spawn set successfully");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Command error: some args are not numbers");
					return true;
				} catch (SiegeException e) {
					sender.sendMessage(ChatColor.RED + "Command error: " + e.getMessage());
					return true;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "Command error: invalid team color");
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					e.printStackTrace();
					return true;
				}
			}
		});

		getCommand("settower").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("Blue", "Red");
				if (args.length == 2)
					return Arrays.asList("<radius>");
				return null;
			}
		});

		getCommand("settower").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (args.length < 2)
					return false;

				// check caller is player
				Player p = checkAndCast(sender);

				try {
					// try to spawn a tower
					spawnTower(p.getLocation(), SiegeColor.valueOf(args[0].toUpperCase()), 10, parseInt(args[1]), 5000,
							0.1, 30, 2000);
					sender.sendMessage(SiegeColor.valueOf(args[0].toUpperCase()) + "" + ChatColor.GREEN
							+ " tower created successfully");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Command error: some args are not numbers");
					return true;
				} catch (SiegeException e) {
					sender.sendMessage(ChatColor.RED + "Command error: " + e.getMessage());
					return true;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "Command error: invalid team color");
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					e.printStackTrace();
					return true;
				}
			}

		});

		getCommand("setppspawn").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("<delay>");
				if (args.length == 2)
					return Arrays.asList("<radius>");
				return null;
			}
		});

		getCommand("setppspawn").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (args.length < 2)
					return false;

				// check caller is player
				Player p = checkAndCast(sender);

				try {
					// try to set powerup spawn point
					setPowerupSpawn(p.getLocation(), (int) parseInt(args[0]), (int) parseInt(args[1]));
					sender.sendMessage(ChatColor.GREEN + "powerup spawn set successfully");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Command error: some args are not numbers");
					return true;
				} catch (SiegeException e) {
					sender.sendMessage(ChatColor.RED + "Command error: " + e.getMessage());
					return true;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "Command error: invalid team color");
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					e.printStackTrace();
					return true;
				}
			}
		});

		getCommand("removeteam").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				ArrayList<String> players = new ArrayList<>();
				Arrays.asList(Bukkit.getOfflinePlayers()).forEach(e -> players.add(e.getName()));
				return players;
			}
		});

		getCommand("removeteam").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (args.length < 1)
					return false;

				// check caller is a player
				checkAndCast(sender);

				try {
					removePlayersTeam(Arrays.asList(args));
					sender.sendMessage(ChatColor.GOLD + "" + Arrays.asList(args) + " have been removed from his team");
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					return true;
				}
			}
		});

		getCommand("setteam").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("Blue", "Red");

				ArrayList<String> players = new ArrayList<>();
				Arrays.asList(Bukkit.getOfflinePlayers()).forEach(e -> players.add(e.getName()));
				return players;
			}
		});

		getCommand("setteam").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (args.length < 2)
					return false;

				// check caller is a player
				checkAndCast(sender);

				try {
					List<String> players = new ArrayList<>(Arrays.asList(args));
					players.remove(0);

					setPlayersTeam(SiegeColor.valueOf(args[0].toUpperCase()), players);
					sender.sendMessage(ChatColor.GOLD + players.toString() + " team set to "
							+ SiegeColor.valueOf(args[0].toUpperCase()));
					return true;
				} catch (SiegeException e) {
					sender.sendMessage(ChatColor.RED + "Command error: " + e.getMessage());
					return true;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "Command error: invalid team color");
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					e.printStackTrace();
					return true;
				}
			}
		});

		getCommand("play").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("<play_time>");
				if (args.length == 2)
					return Arrays.asList("<robot_delay>");

				return null;
			}
		});

		getCommand("play").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// 2 args => start with the custom settings
				if (args.length < 2)
					return false;

				// check caller is player
				checkAndCast(sender);

				try {
					startGame((int) parseInt(args[0]), (int) parseInt(args[1]));
					sender.sendMessage(ChatColor.GREEN + "Game starting...");
					return true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Command error: some args are not numbers");
					return false;
				} catch (SiegeException e) {
					sender.sendMessage(ChatColor.RED + "Command error: " + e.getMessage());
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Internal error: please contact the plugin maker.");
					e.printStackTrace();
					return true;
				}
			}
		});

		getCommand("stop").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
				if (args.length == 1)
					return Arrays.asList("<reason>");
				return null;
			}
		});

		getCommand("stop").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// check caller is player
				checkAndCast(sender);

				if (args.length != 0)
					System.err.println("Game stopped by operator. " + String.join(" ", args));

				// ask server to stop game
				stop();
				getListeners().forEach(e -> e.onTimeExpired(game.getWinning()));
				sender.sendMessage(ChatColor.GOLD + "Game stopped");
				return true;
			}
		});

	}

	/**
	 * on disable plugin method DON'T CALL!!
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	@Override
	public void onDisable() {
		checkThread();
		HandlerList.unregisterAll((JavaPlugin) this);
		instance = null;

	}

	@SuppressWarnings("unchecked")
	private <V> Map<String, V> read(String f) throws IOException, ClassNotFoundException {
		BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(new File(getDataFolder(), f)));
		Map<String, V> tmp = (Map<String, V>) in.readObject();
		in.close();
		return tmp;
	}

	private void write(Map<String, ?> serializedObj, String f) throws IOException {
		File file = new File(getDataFolder(), f);
		if (!file.exists())
			file.createNewFile();

		BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(file));
		out.writeObject(serializedObj);
		out.close();
	}

	@EventHandler
	private void onItemPickupEvent(EntityPickupItemEvent e) {
		if (game.getBlueTower() == null || game.getRedTower() == null)
			return;

		// check if player picked up an emerald (tower powerups)
		if (e.getItem().getItemStack().getType() == Material.EMERALD && e.getEntity() instanceof Player) {
			if (blueTeam.contains(e.getEntity()))
				game.getBlueTower().addPowerups(e.getItem().getItemStack().getAmount());
			if (redTeam.contains(e.getEntity()))
				game.getRedTower().addPowerups(e.getItem().getItemStack().getAmount());

			// send a info message with the amount of powerups picked up
			Player p = (Player) e.getEntity();
			p.sendMessage(ChatColor.GREEN + "+" + e.getItem().getItemStack().getAmount()
					+ (e.getItem().getItemStack().getAmount() > 1 ? " Powerups" : " Powerup"));

			// remove the item from the inventory
			e.getItem().remove();
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onEntityTargetChangeEvent(EntityTargetEvent e) {
		// cancel target change (only code level allowed)
		if (e.getEntity() instanceof Wither)
			e.setCancelled(true);
	}

	@EventHandler
	private void onRobotDamageEvent(EntityDamageEvent e) {
		if (game.getBlueTower() == null || game.getRedTower() == null)
			return;

		// when robot takes damage cancel it and damage it "at game level"
		if (e.getEntity() instanceof Wither) {
			// find the robot associated at the damaged entity
			Robot r = game.getBlueTower().getAllyRobots().get(e.getEntity().getUniqueId());
			if (r == null)
				r = game.getRedTower().getAllyRobots().get(e.getEntity().getUniqueId());

			if (r == null)
				return;

			r.damage((int) e.getDamage());
			e.setDamage(0);
		}
	}

	@EventHandler
	private void onWitherSkullHitEvent(ProjectileHitEvent e) {
		if (game.getBlueTower() == null || game.getRedTower() == null)
			return;

		if (e.getEntity() instanceof WitherSkull) {
			// damage the tower if in range
			Wither w = (Wither) ((WitherSkull) e.getEntity()).getShooter();
			Robot r = game.getBlueTower().getAllyRobots().get(w.getUniqueId());
			if (r == null)
				r = game.getRedTower().getAllyRobots().get(w.getUniqueId());

			if (r == null)
				return;

			// check if wither skull exploded near tower
			if (e.getEntity().getLocation().distance(game.getBlueTower().getTower().getLocation()) < 3)
				game.getBlueTower().damage(r.getDamage());
			if (e.getEntity().getLocation().distance(game.getRedTower().getTower().getLocation()) < 3)
				game.getRedTower().damage(r.getDamage());

			e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 1, false, false);
			e.setCancelled(true);

		}
	}

	@EventHandler
	private void onPlayerDamageEvent(EntityDamageEvent e) {
		// if a player dies wait 5s and the respawn it
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();

			// if player could die cancel it
			if (e.getFinalDamage() >= p.getHealth()) {
				e.setCancelled(true);

				// change gamemode to spectator
				p.setHealth(20);
				p.setGameMode(GameMode.SPECTATOR);
				p.sendTitle(ChatColor.RED + "You are dead!", "", 1, 20, 1);

				// wait 5s before respawn
				new BukkitRunnable() {

					int i = 5;

					@Override
					public void run() {
						if (i == 0) {
							// after 5s change his gamemode to survival and teleport at his own spawn
							p.setGameMode(GameMode.SURVIVAL);
							if (redTeam.contains(e.getEntity()))
								e.getEntity().teleport(game.getRedSpawn());
							if (blueTeam.contains(e.getEntity()))
								e.getEntity().teleport(game.getBlueSpawn());

							SiegeItems.setEquipment(p);

							cancel();
						}

						p.sendTitle(ChatColor.GREEN + "Respawn in: " + ChatColor.WHITE + i-- + "s", "", 1, 20, 1);

					}
				}.runTaskTimer(this, 20, 20);
			}
		}
	}

	@EventHandler
	private void onArrowHitEvent(ProjectileHitEvent e) {
		// remove arrows and if it hit a non protected block delete it
		if (e.getEntity() instanceof Arrow) {
			if (e.getHitBlock() != null && !isProtected(e.getHitBlock()))
				e.getHitBlock().setType(Material.AIR);
			e.getEntity().remove();
		}
	}

	@EventHandler
	private void onBlockExplodeEvent(BlockExplodeEvent e) {
		if (isProtected(e.getBlock()))
			e.setCancelled(true);
	}

	@EventHandler
	private void onTNTExplodeEvent(EntityExplodeEvent e) {
		if (game.getBlueTower() == null || game.getRedTower() == null)
			return;

		// listen for tnt explode
		if (e.getEntity() instanceof TNTPrimed) {
			// damage the tower if in range
			if (e.getEntity().getLocation().distance(game.getBlueTower().getTower().getLocation()) < 10)
				game.getBlueTower().damage(50);
			if (e.getEntity().getLocation().distance(game.getRedTower().getTower().getLocation()) < 10)
				game.getRedTower().damage(50);

			// destroy the blocks placed by players if in range
			e.blockList().forEach(el -> {
				if (!isProtected(el))
					el.setType(Material.AIR);
			});

			e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 1, false, false);
			e.setCancelled(true);
		}

	}

	@EventHandler
	private void onBlockPlaceEvent(BlockPlaceEvent event) {
		// if it's tnt blow it else save it as block placed by player
		if (event.getBlock().getType() == Material.TNT) {
			event.getBlock().setType(Material.AIR);
			TNTPrimed tnt = (TNTPrimed) event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation(),
					EntityType.PRIMED_TNT);
			tnt.setFuseTicks(20);
		} else
			placedBlocks.add(new ImmutableLocation(event.getBlock().getLocation()));
	}

	@EventHandler
	private void onBlockBreakEvent(BlockBreakEvent event) {
		// destroy the block only if the block was placed by a player
		if (isProtected(event.getBlock()))
			event.setCancelled(true);
		else
			placedBlocks.remove(new ImmutableLocation(event.getBlock().getLocation()));

	}

	@EventHandler
	private void onWorldSaveEvent(WorldSaveEvent event) {
		// save the location of tower, powerup area and spawnpoints
		try {
			getDataFolder().mkdir();
			write(game.getRedTower().serialize(), "Red tower.sc");
			write(game.getBlueTower().serialize(), "Blue tower.sc");

			write(game.getRedSpawn().serialize(), "Red spawn.sc");
			write(game.getBlueSpawn().serialize(), "Blue spawn.sc");

			write(game.getPowerupSpawn().serialize(), "Powerup spawn.sc");
			write(game.getSettings(), "Game settings.sc");
		} catch (IOException e) {
			System.err.println("Could not save siegecraft properties. Error: " + e.getMessage());
		}
	}

	@EventHandler
	private void onChatEvent(AsyncPlayerChatEvent event) {
		if (blueTeam.contains(event.getPlayer()))
			Bukkit.getOnlinePlayers().forEach(e -> e.sendMessage(ChatColor.BLUE + "[Blue] "
					+ event.getPlayer().getName() + ": " + ChatColor.GOLD + event.getMessage()));
		else if (redTeam.contains(event.getPlayer()))
			Bukkit.getOnlinePlayers().forEach(e -> e.sendMessage(ChatColor.DARK_RED + "[Red] "
					+ event.getPlayer().getName() + ": " + ChatColor.GOLD + event.getMessage()));
		else
			Bukkit.getOnlinePlayers().forEach(e -> e.sendMessage(ChatColor.DARK_PURPLE + "[Spectator] "
					+ event.getPlayer().getName() + ": " + ChatColor.GOLD + event.getMessage()));

		event.setCancelled(true);

	}

	// check if block was placed by player or not
	private boolean isProtected(Block block) {
		return !placedBlocks.contains(new ImmutableLocation(block.getLocation()));
	}

	private void createCircle(Location position, int radius, Material m) {
		HashSet<Block> blocks = new HashSet<>();

		for (double i = 0.0; i < 360.0; i += 0.05) {
			double angle = i * Math.PI / 180;
			int x = (int) (position.getX() + radius * Math.cos(angle));
			int z = (int) (position.getZ() + radius * Math.sin(angle));
			Location tmp = new Location(position.getWorld(), x, position.getY(), z);
			blocks.add(position.getWorld().getHighestBlockAt(tmp));
		}

		Iterator<Block> iter = blocks.iterator();

		new BukkitRunnable() {

			@Override
			public void run() {
				iter.next().setType(m);

				if (!iter.hasNext())
					cancel();
			}
		}.runTaskTimer(this, 1, 1);

	}

	/**
	 * return the instance of this plugin if enabled else return null
	 * 
	 * @return the plugin instance if enabled null otherwise.
	 */

	public static SiegeCraft getInstance() {
		return instance;
	}

	/**
	 * check if the caller of the method is the bukkit thread. If it's not the
	 * bukkit thread raise {@link IllegalStateException}
	 * 
	 * @throws IllegalStateException if the caller thread is not the bukkit thread
	 */

	public static void checkThread() {
		if (!Bukkit.isPrimaryThread())
			throw new IllegalStateException("Cannot call this method outside bukkit thread");
	}

	private static void onPlayerAdd(Change<? extends Player> e) {
		e.next();
		if (e.wasAdded())
			for (Player p : e.getAddedSubList())
				if (p == null)
					throw new SiegeException("Player not found");
	}

	private static Player checkAndCast(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Command error: this command can be called only by players");
		}

		return (Player) sender;
	}

	public static void main(String[] args) {
	}
}
