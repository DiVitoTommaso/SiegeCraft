package com.siege.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import com.siege.data.Robot;
import com.siege.data.SiegeBoard;
import com.siege.data.Tower;
import com.siege.data.constants.SiegeColor;
import com.siege.data.constants.SiegeItems;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import siege.exeptions.SiegeException;
import siege.listeners.SiegeGameListener;

/**
 * Game driver class
 * 
 * @author Tommaso
 *
 */

final class SiegeGame {

	// list of all listeners
	private final ObservableList<SiegeGameListener> listeners = FXCollections.observableArrayList();

	// game scoreboard
	private final SiegeBoard scoreboard;

	// remaining time and game settings
	private final IntegerProperty currTime;
	private final Map<String, Number> settings;

	// tower of each team
	private Tower blueTower;
	private Tower redTower;

	// location of respawn and powerup spawn
	private Location redSpawn;
	private Location blueSpawn;
	private Location powerupSpawn;

	// timer and powerup drop animation
	private BukkitRunnable timer;
	private BukkitRunnable powerupSpawner;

	private boolean playing;

	public SiegeGame(SiegeBoard board) {
		currTime = new SimpleIntegerProperty();

		// set on time expired
		onTimeChange(e -> {
			listeners.forEach(l -> l.onTimeChange(e.intValue()));
			if (e <= 0) {
				listeners.forEach(l -> l.onTimeExpired(getWinning()));
				stop();
			}
			return null;
		});

		// init settings
		settings = new HashMap<>();
		settings.put("robotSpawnDelay", 180);
		settings.put("powerupSpawnRadius", 50);
		settings.put("powerupSpawnDelay", 30);
		settings.put("maxPlayTime", 1200);

		this.scoreboard = board;
	}

	public void play() {
		if (playing)
			throw new SiegeException("Game already running");

		// init the game
		scoreboard.enable();
		currTime.set(settings.get("maxPlayTime").intValue());

		powerupSpawner = new BukkitRunnable() {

			@Override
			public void run() {
				int powerupSpawnRadius = settings.get("powerupSpawnRadius").intValue();

				double r = powerupSpawnRadius * Math.sqrt(Math.random());
				double theta = Math.random() * 2 * Math.PI;

				double xadd = r * Math.cos(theta);
				double zadd = r * Math.sin(theta);

				// spawn a new powerup in a random position inside the powerup spawn area
				powerupSpawn.add(xadd, 30, zadd);

				Item item = powerupSpawn.getWorld().dropItemNaturally(powerupSpawn, SiegeItems.POWERUP());

				// set cutom item information
				item.setGlowing(true);
				item.setCustomName(ChatColor.GREEN + "POWERUP");
				item.setCustomNameVisible(true);

				// create spawn aniamtion and sound
				spawnFollowingFirework(item);

				getListeners().forEach(e -> e.onPowerupSpawn(item));

				powerupSpawn.add(-xadd, -30, -zadd);
			}
		};

		int powerupTickRate = settings.get("powerupSpawnDelay").intValue() * 20;
		powerupSpawner.runTaskTimer(SiegeCraft.getInstance(), powerupTickRate, powerupTickRate);

		timer = new BukkitRunnable() {

			private int robotSpawnDelay = settings.get("robotSpawnDelay").intValue();
			private int powerupSpawnDelay = settings.get("powerupSpawnDelay").intValue();

			@Override
			public void run() {

				// decrease timer by 1 secod every 20 ticks
				currTime.set(currTime.get() - 1);

				// update scoreboard time
				scoreboard.setTime(
						ChatColor.translateAlternateColorCodes('&', "&5Game ends: &a" + secondsToStr(currTime.get())));

				// update scoreboard robot spawn delay
				if (robotSpawnDelay == 0)
					robotSpawnDelay = settings.get("robotSpawnDelay").intValue();
				else
					robotSpawnDelay--;

				// update scorebaord next robot
				scoreboard.setRobotSpawnTime(ChatColor.translateAlternateColorCodes('&',
						"&5Next Robot: &a" + secondsToStr(robotSpawnDelay)));

				// update scoreboard next powerup
				if (powerupSpawnDelay == 0)
					powerupSpawnDelay = settings.get("powerupSpawnDelay").intValue();
				else
					powerupSpawnDelay--;

				scoreboard.setPowerupSpawnTime(ChatColor.translateAlternateColorCodes('&',
						"&5Next Powerup: &a" + secondsToStr(powerupSpawnDelay)));

				if (robotSpawnDelay == 0)
					// spawn robot at fixed delay for the tower with most powerups
					if (blueTower.getPowerups() > redTower.getPowerups()) {
						int powerups = blueTower.getPowerups();
						Robot r = blueTower.createRobot(redTower);
						listeners.forEach(e -> e.onRobotSpawn(SiegeColor.BLUE, r, powerups));

					} else if (blueTower.getPowerups() < redTower.getPowerups()) {
						int powerups = redTower.getPowerups();
						Robot r = redTower.createRobot(blueTower);
						listeners.forEach(e -> e.onRobotSpawn(SiegeColor.RED, r, powerups));

					} else
						// on draw set all to null and level to -1
						listeners.forEach(e -> e.onRobotSpawn(null, null, -1));

			}
		};
		timer.runTaskTimer(SiegeCraft.getInstance(), 20, 20);

		blueTower.start();
		redTower.start();

		// run the listeners on game start
		listeners.forEach(e -> e.onGameStart(blueTower, redTower));

		playing = true;
	}

	/**
	 * ask to stop game
	 */

	private void stop() {
		blueTower.stop();
		redTower.stop();

		powerupSpawner.cancel();
		timer.cancel();

		playing = false;
	}

	/**
	 * ask to expire the timer
	 */
	public void expireTimer() {
		currTime.set(1);
	}

	/**
	 * check if game is valid
	 */
	public void checkState() {
		if (blueTower == null)
			throw new SiegeException("Missing Blue tower");

		if (redTower == null)
			throw new SiegeException("Missing Red tower");

		if (blueSpawn == null)
			throw new SiegeException("Missing Blue spawn");

		if (blueSpawn == null)
			throw new SiegeException("Missing Red spawn");

		if (powerupSpawn == null)
			throw new SiegeException("Missing powerup spawn area");
	}

	/**
	 * get current winning team color
	 * 
	 * @return the team color or null if draw
	 */
	public SiegeColor getWinning() {
		return blueTower.getHealth() > redTower.getHealth() ? SiegeColor.BLUE
				: blueTower.getHealth() < redTower.getHealth() ? SiegeColor.RED : null;
	}

	/**
	 * run on time change the function f
	 * 
	 * @param f the function to run with the new time value
	 * @return
	 */

	public void onTimeChange(Function<Integer, Void> f) {
		currTime.addListener((o, old, neww) -> f.apply(neww.intValue()));
	}

	/**
	 * get blue tower
	 * 
	 * @return
	 */

	public Tower getBlueTower() {
		return blueTower;
	}

	/**
	 * set blue tower
	 * 
	 * @param blueTower
	 */

	public void setBlueTower(Tower blueTower) {
		if (this.blueTower != null)
			this.blueTower.getTower().remove();

		blueTower.onTowerDamage(() -> {
			listeners.forEach(el -> el.onTowerDamage(SiegeColor.BLUE, blueTower));
			if (blueTower.getHealth() <= 0)
				stop();
		});

		blueTower.onPowerupChange(e -> {
			listeners.forEach(el -> el.onPowerupsChange(SiegeColor.BLUE, blueTower));
			return null;
		});
		this.blueTower = blueTower;

	}

	/**
	 * get red tower
	 * 
	 * @return
	 */

	public Tower getRedTower() {
		return redTower;
	}

	/**
	 * set red tower
	 * 
	 * @param redTower
	 */

	public void setRedTower(Tower redTower) {
		if (this.redTower != null)
			this.redTower.getTower().remove();

		redTower.onTowerDamage(() -> {
			listeners.forEach(el -> el.onTowerDamage(SiegeColor.RED, redTower));
			if (redTower.getHealth() <= 0)
				stop();
		});

		redTower.onPowerupChange(e -> {
			listeners.forEach(el -> el.onPowerupsChange(SiegeColor.RED, redTower));
			return null;
		});
		this.redTower = redTower;
	}

	/**
	 * get game listeners
	 * 
	 * @return
	 */

	public ObservableList<SiegeGameListener> getListeners() {
		return listeners;
	}

	/**
	 * get settings map
	 * 
	 * @return
	 */

	public Map<String, Number> getSettings() {
		return settings;
	}

	/**
	 * get blue spawn
	 * 
	 * @return
	 */

	public Location getBlueSpawn() {
		return blueSpawn;
	}

	/**
	 * set blue spawn
	 * 
	 * @param blueSpawn
	 */

	public void setBlueSpawn(Location blueSpawn) {
		this.blueSpawn = blueSpawn;
	}

	/**
	 * get red spawn
	 * 
	 * @return
	 */

	public Location getRedSpawn() {
		return redSpawn;
	}

	/**
	 * set red spawn
	 * 
	 * @param redSpawn
	 */

	public void setRedSpawn(Location redSpawn) {
		this.redSpawn = redSpawn;
	}

	/**
	 * get powerup spawn
	 * 
	 * @return
	 */

	public Location getPowerupSpawn() {
		return powerupSpawn;
	}

	/**
	 * set powerup spawn
	 * 
	 * @param powerupSpawn
	 */

	public void setPowerupSpawn(Location powerupSpawn) {
		this.powerupSpawn = powerupSpawn;
	}

	/**
	 * check if game is running
	 * 
	 * @return
	 */

	public boolean isPlaying() {
		return playing;
	}

	private static void spawnFollowingFirework(Item item) {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (item.isOnGround() || item.isDead())
					cancel();

				// create sound and trail for powerup
				Firework f = (Firework) item.getWorld().spawnEntity(item.getLocation(), EntityType.FIREWORK);
				f.detonate();
				Particle.DustOptions dust = new Particle.DustOptions(Color.GREEN, 5);
				item.getWorld().spawnParticle(Particle.REDSTONE, item.getLocation(), 5, 0, 0, 0, 1, dust);

			}

		}.runTaskTimer(SiegeCraft.getInstance(), 1, 1);

	}

	private String secondsToStr(int totalSeconds) {
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		return (minutes > 0 ? minutes + "m" : "") + (seconds < 10 ? "0" + seconds : seconds) + "s";
	}

}
