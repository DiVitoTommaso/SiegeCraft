package com.siege.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.siege.core.SiegeCraft;

import javafx.collections.ObservableList;
import siege.util.BukkitRestricted;

@BukkitRestricted
public final class SiegeBoard implements Listener {

	private ObservableList<Player> redTeam;
	private ObservableList<Player> blueTeam;

	private Scoreboard scoreboard;
	private Objective siege;

	private Team blue;
	private Team red;

	private boolean initialized;

	public SiegeBoard(ObservableList<Player> redTeam, ObservableList<Player> blueTeam) {
		if (redTeam == null || blueTeam == null)
			throw new IllegalArgumentException("Siege board players' lists must be not null");

		this.blueTeam = blueTeam;
		this.redTeam = redTeam;
	}

	/**
	 * internal method used to initialize the scoreboard. Calls to this method won't
	 * make any effect on the game
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void init() {
		if (initialized)
			return;

		Bukkit.getPluginManager().registerEvents(this, SiegeCraft.getInstance());

		// init scoreboard
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		// create red and blue teams
		red = scoreboard.registerNewTeam("Red");
		blue = scoreboard.registerNewTeam("Blue");

		// do'nt allow friendly fire
		red.setAllowFriendlyFire(false);
		blue.setAllowFriendlyFire(false);

		// set tab color name
		blue.setColor(ChatColor.BLUE);
		red.setColor(ChatColor.RED);

		// register new list
		siege = scoreboard.registerNewObjective("Time", "dummy", ChatColor.GOLD + "SiegeCraft");

		// add players to the teams
		blueTeam.forEach(e -> blue.addEntry(e.getName()));
		redTeam.forEach(e -> red.addEntry(e.getName()));

		initialized = true;

	}

	/**
	 * prepare the scoreboard with the basic informations. This method should be
	 * called onGameStart() listener's method. (The SiegeBaselistener will make it
	 * for you)
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void enable() {
		SiegeCraft.checkThread();

		siege.setDisplaySlot(DisplaySlot.SIDEBAR);
		siege.setRenderType(RenderType.INTEGER);

		siege.getScore(" ".repeat(20)).setScore(11);
		siege.getScore(" ".repeat(2)).setScore(7);
		siege.getScore(ChatColor.BLUE + "Blue Tower").setScore(6);
		siege.getScore(" ".repeat(1)).setScore(3);
		siege.getScore(ChatColor.DARK_RED + "Red Tower").setScore(2);
	}

	@EventHandler
	private void onPlayerJoinEvent(PlayerJoinEvent e) {
		// when player join the server set the scoreboard
		e.getPlayer().setScoreboard(scoreboard);
	}

	@EventHandler
	private void onPlayerLeaveEvent(PlayerQuitEvent e) {
		// when player leave the server remove the scoreboard
		e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	private String prevTime = "";

	/**
	 * change remaining time until game end on the scoreboard
	 * 
	 * @param newPowerups new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setTime(String newTime) {
		SiegeCraft.checkThread();

		// update time
		scoreboard.resetScores(prevTime);
		siege.getScore(prevTime = newTime).setScore(10);
	}

	private String prevRobotSpawnTime = "";

	/**
	 * change remaining time until next robot spawn on the scoreboard
	 * 
	 * @param newRobotSpawnTime new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setRobotSpawnTime(String newRobotSpawnTime) {
		SiegeCraft.checkThread();

		scoreboard.resetScores(prevRobotSpawnTime);
		siege.getScore(prevRobotSpawnTime = newRobotSpawnTime).setScore(9);
	}

	private String prevPowerupSpawnTime = "";

	/**
	 * change remaining time until next powerup spawn on the scoreboard
	 * 
	 * @param newPowerupSpawnTime new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setPowerupSpawnTime(String newPowerupSpawnTime) {
		SiegeCraft.checkThread();

		scoreboard.resetScores(prevPowerupSpawnTime);
		siege.getScore(prevPowerupSpawnTime = newPowerupSpawnTime).setScore(8);
	}

	private String prevBlueHealth = "";

	/**
	 * change blue towers's health on the scoreboard
	 * 
	 * @param newHealth new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setBlueTowerHealth(String newHealth) {
		SiegeCraft.checkThread();

		scoreboard.resetScores(prevBlueHealth);
		siege.getScore(prevBlueHealth = newHealth).setScore(5);
	}

	private String prevBluePowerups = "";

	/**
	 * change blue tower's powerups amount on the scoreboard
	 * 
	 * @param newPowerups new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setBlueTowerPowerups(String newPowerups) {
		SiegeCraft.checkThread();

		scoreboard.resetScores(prevBluePowerups);
		siege.getScore(prevBluePowerups = newPowerups).setScore(4);
	}

	private String prevRedHealth = "";

	/**
	 * change the red tower's health on the scoreboard
	 * 
	 * @param newHealth new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setRedTowerHealth(String newHealth) {
		SiegeCraft.checkThread();

		scoreboard.resetScores(prevRedHealth);
		siege.getScore(prevRedHealth = newHealth).setScore(1);
	}

	private String prevRedPowerups = "";

	/**
	 * change red tower's powerups amount on the scoreboard
	 * 
	 * @param newPowerups new line to be replaced with old one
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void setRedTowerPowerups(String newPowerups) {
		SiegeCraft.checkThread();

		scoreboard.resetScores(prevRedPowerups);
		siege.getScore(prevRedPowerups = newPowerups).setScore(0);
	}

	/**
	 * clear the scoreboard. This method should be called when game ends. (The
	 * SiegeBaseListener will make it for you)
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void clear() {
		SiegeCraft.checkThread();

		scoreboard.clearSlot(DisplaySlot.SIDEBAR);

	}
}
