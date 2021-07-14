package com.siege.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.scheduler.BukkitRunnable;

import com.siege.core.SiegeCraft;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import siege.util.BukkitRestricted;

@BukkitRestricted
public final class Robot {

	private final Tower owner;
	private final Tower enemy;

	private final Wither robot;
	private final BossBar robotBossBar;
	private final int damage;

	private final IntegerProperty health;
	private final BukkitRunnable attackAI;

	/**
	 * build a full customizable robot
	 * 
	 * @param ally      ally tower
	 * @param enemy     enemy tower
	 * @param damage    robot damage per hit
	 * @param maxHealth robot health
	 */

	Robot(Tower ally, Tower enemy, int damage, int maxHealth) {

		this.owner = ally;
		this.enemy = enemy;

		this.health = new SimpleIntegerProperty(maxHealth);
		this.robot = (Wither) ally.getTower().getWorld().spawnEntity(ally.getTower().getLocation(), EntityType.WITHER);
		this.robot.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);

		this.robot.setAI(true);
		this.robot.setGlowing(true);
		this.robot.getBossBar().setVisible(false);

		this.robot.setTarget(enemy.getTower());

		this.damage = damage;
		this.robotBossBar = Bukkit.createBossBar(
				ChatColor.translateAlternateColorCodes('&', "&6&lRobot health: &a" + maxHealth + " &4❤"),
				BarColor.PURPLE, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);

		Bukkit.getOnlinePlayers().forEach(e -> robotBossBar.addPlayer(e));

		onRobotDamage(() -> {
			if (getHealth() <= 0)
				Bukkit.getOnlinePlayers().forEach(e -> robotBossBar.removePlayer(e));
			else {
				robotBossBar.setTitle(
						ChatColor.translateAlternateColorCodes('&', "&6&lRobot health: &a" + getHealth() + " &4❤"));
				robotBossBar.setProgress((double) getHealth() / maxHealth);
			}
		});

		// until death focus tower, but if enemy players get closer attack it
		attackAI = new BukkitRunnable() {

			@Override
			public void run() {
				// if robot is dead stop the robot AI
				if (isDead()) {
					robot.remove();
					cancel();
				}

				// if some players are near the robot damage them
				for (Player p : ally.getEnemies())
					if (robot.getLocation().distance(p.getLocation()) < 10 && p.getGameMode() == GameMode.SURVIVAL) {
						robot.setTarget(p);
						return;
					}

				if (robot.getTarget() != enemy.getTower())
					robot.setTarget(enemy.getTower());

			}
		};

		attackAI.runTaskTimer(SiegeCraft.getInstance(), 20, 20);
	}

	/**
	 * stop the robot.
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void stop() {
		SiegeCraft.checkThread();
		damage(health.get());
	}

	/**
	 * get the damage inflicted per hit
	 * 
	 * @return the damage per hit
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public int getDamage() {
		SiegeCraft.checkThread();
		return damage;

	}

	/**
	 * get the minecraft entity associated to this instance
	 * 
	 * @return the entity associated
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public Wither getRobot() {
		SiegeCraft.checkThread();
		return robot;
	}

	/**
	 * damage the robot for the specified amount
	 * 
	 * @param damage the damage to inflict to the robot
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void damage(int damage) {
		SiegeCraft.checkThread();
		health.set(health.get() - damage);
	}

	/**
	 * get the current remaining health
	 * 
	 * @return the current health of the robot
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public int getHealth() {
		SiegeCraft.checkThread();
		return health.get();
	}

	/**
	 * call function on robot damage
	 * 
	 * @param f function to be called on robot damage
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public void onRobotDamage(Runnable f) {
		SiegeCraft.checkThread();
		health.addListener((o, old, neww) -> f.run());
	}

	/**
	 * get graphic boss bar. Note: don't change the wither properties. Changes on
	 * wither health/ bossbar won't do any change to the robot and they could cause
	 * some errors
	 * 
	 * @param the color of the bossbar
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public void setBossBarColor(BarColor c) {
		SiegeCraft.checkThread();
		robotBossBar.setColor(c);
	}

	/**
	 * get the owner tower
	 * 
	 * @return the tower owner
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public Tower getOwner() {
		SiegeCraft.checkThread();
		return owner;
	}

	/**
	 * get the enemy tower
	 * 
	 * @return the tower enemy of this robot
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */

	public Tower getEnemy() {
		SiegeCraft.checkThread();
		return enemy;
	}

	/**
	 * check if robot is dead
	 * 
	 * @return true if the health of the robot is <= 0 false otherwise
	 * 
	 * @throws IllegalStateException if the caller is not the bukkit thread
	 */
	public boolean isDead() {
		SiegeCraft.checkThread();
		return health.get() <= 0;
	}
}
