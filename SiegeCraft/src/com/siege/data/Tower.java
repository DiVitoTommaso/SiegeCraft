package com.siege.data;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.io.BukkitObjectInputStream;

import com.siege.core.SiegeCraft;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import siege.exeptions.SiegeException;
import siege.util.BukkitRestricted;

@BukkitRestricted
public final class Tower {

	// game data
	private final ObservableMap<UUID, Robot> allyRobots;
	private final ObservableMap<UUID, Robot> enemyRobots;

	private final ObservableList<Player> enemies;
	private final ObservableList<Player> allies;

	// graphic object
	private final ArmorStand tower;
	private final SimpleIntegerProperty currHealth;

	// tower properties
	private final int maxHealth;
	private final int damage;
	private final int radius;

	private final double robotLevelMultiplier;
	private final int robotBaseDamage;
	private final int robotBaseHealth;

	private final IntegerProperty powerups;

	private BukkitRunnable shootAI;

	/**
	 * create full customizable tower
	 * 
	 * @param allies               observable list of allies
	 * @param enemies              observable list of enemies
	 * @param position             location of the tower
	 * @param damage               damage per tick
	 * @param radius               tower damage area
	 * @param health               tower health
	 * @param robotLevelMultiplier robot base stats multiplier
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public Tower(ObservableList<Player> allies, ObservableList<Player> enemies, Location position, int damage,
			int radius, int health, double robotLevelMultiplier, int robotbaseDamage, int robotBaseHealth) {

		SiegeCraft.checkThread();

		this.allies = FXCollections.unmodifiableObservableList(allies);
		this.enemies = FXCollections.unmodifiableObservableList(enemies);

		this.enemyRobots = FXCollections.observableHashMap();
		this.allyRobots = FXCollections.observableHashMap();

		this.powerups = new SimpleIntegerProperty(0);

		position.add(0, -1, 0);
		position.getBlock().setType(Material.BEDROCK);
		position.add(0, 1, 0);

		this.tower = (ArmorStand) position.getWorld().spawnEntity(position, EntityType.ARMOR_STAND);
		this.tower.setGravity(false);
		this.tower.setVisible(false);

		this.currHealth = new SimpleIntegerProperty(health);

		this.maxHealth = health;
		this.damage = damage;
		this.radius = radius;

		this.robotBaseDamage = robotbaseDamage;
		this.robotLevelMultiplier = robotLevelMultiplier;
		this.robotBaseHealth = robotBaseHealth;

		onTowerDamage(() -> {
			if (getHealth() <= 0) {
				allyRobots.values().forEach(e -> e.stop());
				powerups.set(0);
			}
		});
	}

	private boolean playing = false;

	/**
	 * enable tower
	 * 
	 * @throws IllegalStateException if tower has been already enabled or if the
	 *                               caller is not the bukkit thread
	 */
	public void start() {
		SiegeCraft.checkThread();
		if (playing)
			throw new IllegalStateException("Instance already running");

		currHealth.set(maxHealth);

		Location towerFace = tower.getLocation().clone();
		towerFace.add(0, 3, 0);

		shootAI = new BukkitRunnable() {

			@Override
			public void run() {

				if (isDestroyed())
					cancel();

				// check if some one is in the turret range
				Entity target = null;
				double min = Double.MAX_VALUE;
				double curr = 0;

				// find the nearest target
				for (Player p : enemies)
					if ((curr = towerFace.distance(p.getLocation())) < radius && (p.getGameMode() == GameMode.SURVIVAL))
						if (curr < min) {
							min = curr;
							target = p;
						}

				for (Robot r : enemyRobots.values())
					if ((curr = towerFace.distance(r.getRobot().getLocation())) < radius)
						if (curr < min) {
							min = curr;
							target = r.getRobot();
						}

				// take the nearest target and shoot him
				if (target != null) {
					Arrow arrow = tower.getWorld().spawnArrow(towerFace,
							target.getLocation().toVector().subtract(towerFace.toVector()), 6, 1);
					arrow.setDamage(damage);
					arrow.setFireTicks(10);
					arrow.setKnockbackStrength(0);
					arrow.setGravity(false);

				}

			}
		};

		// check peridiocally for enemy in the tower range
		shootAI.runTaskTimer(SiegeCraft.getInstance(), 10, 10);

		playing = true;

	}

	/**
	 * disable tower
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void stop() {
		SiegeCraft.checkThread();
		damage(currHealth.get());
		playing = false;
	}

	/**
	 * run function on tower damage
	 * 
	 * @param f function to be called on tower damage
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void onTowerDamage(Runnable f) {
		SiegeCraft.checkThread();
		currHealth.addListener((o, old, neww) -> f.run());
	}

	/**
	 * add the speciefied amount of emeralds to increase the robot power
	 * 
	 * @param count the number of powerups to be added
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void addPowerups(int count) {
		SiegeCraft.checkThread();
		powerups.set(powerups.get() + count);
	}

	/**
	 * get the current amout of powerups
	 * 
	 * @return the amount of powerups
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public int getPowerups() {
		SiegeCraft.checkThread();
		return powerups.get();
	}

	/**
	 * run function on powerup change
	 * 
	 * @param f function to be called on tower damage
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void onPowerupChange(Function<Double, Void> f) {
		SiegeCraft.checkThread();
		powerups.addListener((o, old, neww) -> f.apply(neww.doubleValue()));
	}

	/**
	 * create a robot using tower constructor stats. The robot stats will be
	 * multiplied by constant based on powerups count. Only speed and damage change,
	 * if you want to change other attributes use the returned instance
	 * 
	 * @param enemy enemy tower
	 * @return the robot just created
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public Robot createRobot(Tower enemy) {
		SiegeCraft.checkThread();

		return createRobot(enemy, robotBaseDamage, robotBaseHealth);
	}

	/**
	 * create a robot given the base stats. The robot stats will be multiplied by
	 * constant based on powerups count. Only speed and damage change, if you want
	 * to change other attributes use the returned instance
	 * 
	 * @param enemy  enemy tower
	 * @param damage base robot damage
	 * @return the robot just created
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public Robot createRobot(Tower enemy, int damage, int health) {
		SiegeCraft.checkThread();

		Robot r = new Robot(this, enemy, (int) (damage + damage * getMultiply()),
				(int) (health + health * getMultiply()));

		r.onRobotDamage(() -> {
			if (r.getHealth() <= 0) {
				allyRobots.remove(r.getRobot().getUniqueId());
				enemy.enemyRobots.remove(r.getRobot().getUniqueId());
			}
		});
		this.allyRobots.put(r.getRobot().getUniqueId(), r);
		enemy.enemyRobots.put(r.getRobot().getUniqueId(), r);
		addPowerups(-powerups.get());
		return r;

	}

	/**
	 * get the listenable map of enemy robots alive
	 * 
	 * @return an unmodifiable map of robots
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public ObservableMap<UUID, Robot> getEnemyRobots() {
		SiegeCraft.checkThread();
		return FXCollections.unmodifiableObservableMap(enemyRobots);
	}

	/**
	 * get the listenable map of ally robots alive
	 * 
	 * @return an unmodifiable map of robots
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public ObservableMap<UUID, Robot> getAllyRobots() {
		SiegeCraft.checkThread();
		return FXCollections.unmodifiableObservableMap(allyRobots);
	}

	// get the multiply for the curret amount of powerups
	private double getMultiply() {
		return powerups.get() * robotLevelMultiplier;
	}

	/**
	 * damage the tower of the given damage
	 * 
	 * @param damage
	 * 
	 * @throws IllegalStateException if caller is not the bukkit thread
	 */
	public void damage(int damage) {
		SiegeCraft.checkThread();
		currHealth.set(currHealth.get() - damage);
	}

	/**
	 * get the health remaining of the tower
	 * 
	 * @return the health of the tower
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public int getHealth() {
		SiegeCraft.checkThread();
		return currHealth.get();
	}

	/**
	 * check if turret is destroyed
	 * 
	 * @return true if health is <=0 false otherwise
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public boolean isDestroyed() {
		SiegeCraft.checkThread();
		return currHealth.get() <= 0;
	}

	/**
	 * get the entity associated at this tower. Note: don't use the armour stand
	 * properties. Changes on armour stand won't do any change to the tower and they
	 * could cause some errors
	 * 
	 * @return the entity associated
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public ArmorStand getTower() {
		SiegeCraft.checkThread();
		return tower;
	}

	/**
	 * get the list of enemy players
	 * 
	 * @return an unmodifable observable list of players
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public ObservableList<Player> getEnemies() {
		SiegeCraft.checkThread();
		return enemies;
	}

	/**
	 * get list of ally players
	 * 
	 * @return an unmodifable observable list of players
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public ObservableList<Player> getAllies() {
		SiegeCraft.checkThread();
		return allies;
	}

	/**
	 * check if the robot is an enemy robot
	 * 
	 * @param r the robot to be check
	 * @return true if it's ane nemy false otherwise
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public boolean isEnemyRobot(UUID r) {
		SiegeCraft.checkThread();
		return enemyRobots.containsKey(r);
	}

	/**
	 * serialize this instance into map
	 * 
	 * @return the serialized instance
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 * 
	 */
	public Map<String, Object> serialize() {
		SiegeCraft.checkThread();

		HashMap<String, Object> tmp = new HashMap<>();
		tmp.put("position", tower.getLocation().serialize());
		tmp.put("maxHealth", maxHealth);
		tmp.put("damage", damage);
		tmp.put("radius", radius);
		tmp.put("robotBaseDamage", robotBaseDamage);
		tmp.put("robotLevelMultiplier", robotLevelMultiplier);
		tmp.put("robotHealth", robotBaseHealth);

		return tmp;
	}

	/**
	 * build tower from file
	 * 
	 * @param enemies observable list of enemies
	 * @param tower   file containing the tower informations
	 * @return a new tower built using file values
	 * @throws SiegeException if a IO error occur
	 */

	@SuppressWarnings("unchecked")
	public static Tower fromFile(ObservableList<Player> allies, ObservableList<Player> enemies, File tower) {
		SiegeCraft.checkThread();

		try {
			BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(tower));
			Map<String, Object> tmp = (Map<String, Object>) in.readObject();
			Map<String, Object> pos = (Map<String, Object>) tmp.get("position");
			int damage = (int) tmp.get("damage");
			int radius = (int) tmp.get("radius");
			int maxHealth = (int) tmp.get("maxHealth");
			double rlm = (double) tmp.get("robotLevelMultiplier");
			int rbd = (int) tmp.get("robotBaseDamage");
			int rh = (int) tmp.get("robotHealth");

			in.close();

			Location loc = Location.deserialize(pos);
			loc.getWorld().getNearbyEntities(new BoundingBox(1, 1, 1, 1, 1, 1)).forEach(e -> e.remove());
			return new Tower(allies, enemies, loc, damage, radius, maxHealth, rlm, rbd, rh);
		} catch (Exception e) {
			throw new SiegeException("Tower file corrupted.");
		}
	}

}
