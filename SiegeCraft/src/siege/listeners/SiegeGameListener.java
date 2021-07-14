package siege.listeners;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.siege.core.SiegeCraft;
import com.siege.data.Robot;
import com.siege.data.SiegeBoard;
import com.siege.data.Tower;
import com.siege.data.constants.SiegeColor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import siege.exeptions.SiegeException;
import siege.util.BukkitRestricted;

/**
 * listener for Siege events
 * 
 * @author Tommaso
 *
 */
@BukkitRestricted
public abstract class SiegeGameListener {

	protected final ObservableList<Player> redTeam;
	protected final ObservableList<Player> blueTeam;
	protected final SiegeBoard scoreboard;

	public SiegeGameListener(SiegeBoard scoreboard, ObservableList<Player> redTeam, ObservableList<Player> blueTeam) {
		if (redTeam == null || blueTeam == null)
			throw new SiegeException("Null values are not allowed");

		this.scoreboard = scoreboard;
		this.redTeam = FXCollections.unmodifiableObservableList(redTeam);
		this.blueTeam = FXCollections.unmodifiableObservableList(blueTeam);
	}

	/**
	 * event fired when time run out
	 * 
	 * @param winner the winner color team
	 * @param red    the list of player composing red team
	 * @param blue   the list of player composing blue team
	 */
	public void onTimeExpired(SiegeColor winner) {
		;
	}

	/**
	 * event fired when a robot spawn. Robots created by commands cannot be accessed
	 * and this method let you access them. For robots created using
	 * {@link SiegeCraft#spawnRobot(org.bukkit.Location, SiegeColor, double, double)}
	 * this method is a short version of
	 * 
	 * <pre>
	 * Robot r = SiegeCraft.getInstance().spawnRobot();
	 * r.onRobotDamage();
	 * 
	 * </pre>
	 * 
	 * 
	 * @param owner owner of the robot
	 * @param enemy enemy tower of the robot
	 * @param r     the robot spawned
	 * @param level the level of the robot (amount of powerup used to spawn it). if
	 *              robot is spawned by method the level will be 0
	 */
	public void onRobotSpawn(SiegeColor owner, Robot r, int level) {
		;
	}

	/**
	 * event fired when a robot takes damage. Short version of
	 * 
	 * <pre>
	 * {@link SiegeGameListener#onRobotSpawn(SiegeColor, Robot, int)} {
	 * 	r.onRobotDamage(functionToBeCalledOnDamage);
	 * }
	 * </pre>
	 * 
	 * Keep in mind that if the game ends for any reason every robot will be killed
	 * and will get a damage amount equal to his health and onRobotDamage() will be
	 * called with 0 as health
	 * 
	 * @param ownerTeam the owner of the robot
	 * @param r         the robot which got damaged
	 */
	public void onRobotDamage(SiegeColor ownerTeam, Robot r) {
		;
	}

	/**
	 * event fired when a tower takes damage. Towers created by commands can be
	 * accessed using onGameStart() and this method is a short way to add a
	 * tower.onTowerDamage() event. For towers created using
	 * {@link SiegeCraft#spawnTower(org.bukkit.Location, SiegeColor, double, double, double, double, double, double)},
	 * this method is a short version of
	 * 
	 * <pre>
	 * Tower t = SiegeCraft.getInstance().spawnTower();
	 * t.onTowerDamage();
	 * 
	 * </pre>
	 * 
	 * @param color the tower color
	 * @param tower the tower which got damage
	 */

	public void onTowerDamage(SiegeColor color, Tower tower) {
		;
	}

	/**
	 * event fired when game starts. This method will be called when everything is
	 * ready to start the game. Please don't use
	 * 
	 * <pre>
	 * SiegeCraft.getInstance().startGame(int, int);
	 * myFunction().
	 * </pre>
	 * 
	 * use
	 * 
	 * <pre>
	 * class mylistener extends SiegeGameListener {
	 * 	public void onGameStart(Tower blue, Tower red) {
	 * 		myFunction();
	 * 	}
	 * }
	 * 
	 * </pre>
	 * 
	 * @param blue the blue tower
	 * @param red  the red tower
	 */
	public void onGameStart(Tower blue, Tower red) {
		;
	}

	/**
	 * event fired when tower powerups amount change. Towers created by commands
	 * cannot be accessed and this method let you access them. For towers created
	 * using
	 * {@link SiegeCraft#spawnTower(org.bukkit.Location, SiegeColor, double, double, double, double, double, double)},
	 * this method is a short version of
	 * 
	 * <pre>
	 * Tower t = SiegeCraft.getInstance().spawnTower();
	 * t.onPowerupsChange();
	 * </pre>
	 * 
	 * @param color color of the tower where powerups changed
	 * @param tower tower where powerups changed
	 */

	public void onPowerupsChange(SiegeColor color, Tower tower) {
		;
	}

	/**
	 * event fire when a new powerup spawns. Powerups cannot be accessed and this
	 * method let you access them. This is not a short way for any method, it's the
	 * only wait to edit the item if you need
	 * 
	 * @param item the powerups just spawned
	 */

	public void onPowerupSpawn(Item item) {
		;
	}

	/**
	 * event fired every time that timer changes (every second)
	 * 
	 * @param newTime
	 */

	public void onTimeChange(int newTime) {
		;
	}

}
