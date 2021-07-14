package siege.listeners;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.siege.data.Robot;
import com.siege.data.SiegeBoard;
import com.siege.data.Tower;
import com.siege.data.constants.SiegeColor;
import com.siege.data.constants.SiegeItems;

import javafx.collections.ObservableList;

/**
 * base listener class
 * 
 * @author Tommaso
 *
 */
public class SiegeBaseListener extends SiegeGameListener {

	private boolean playing = false;

	public SiegeBaseListener(SiegeBoard scoreboard, ObservableList<Player> redTeam, ObservableList<Player> blueTeam) {
		super(scoreboard, redTeam, blueTeam);
	}

	@Override
	public void onTimeExpired(SiegeColor winner) {
		if (!playing)
			return;

		if (winner != null)
			Bukkit.getOnlinePlayers().forEach(e -> e.sendTitle(winner + " team won!", "", 1, 20, 1));
		else
			Bukkit.getOnlinePlayers().forEach(e -> e.sendTitle(ChatColor.GOLD + " Game draw!", "", 1, 20, 1));

		blueTeam.forEach(e -> SiegeItems.setEquipment(e));
		redTeam.forEach(e -> SiegeItems.setEquipment(e));

		playing = false;
	}

	@Override
	public void onRobotSpawn(SiegeColor teamOwner, Robot r, int level) {
		if (teamOwner == null || r == null) {
			Bukkit.getOnlinePlayers().forEach(e -> e.sendMessage(ChatColor.GOLD + "Powerup draw! No robot spawned"));
			return;
		}

		if (teamOwner == SiegeColor.RED)
			redTeam.forEach(e -> e.getInventory().addItem(SiegeItems.BOMB()));
		else
			blueTeam.forEach(e -> e.getInventory().addItem(SiegeItems.BOMB()));

		r.setBossBarColor(teamOwner == SiegeColor.BLUE ? BarColor.BLUE : BarColor.RED);

		Bukkit.getOnlinePlayers().forEach(e -> e.sendMessage(teamOwner + " team spawned a level " + level + " robot!"));
	}

	@Override
	public void onTowerDamage(SiegeColor color, Tower t) {
		if (t.isDestroyed()) {
			onTimeExpired(color == SiegeColor.BLUE ? SiegeColor.RED : SiegeColor.BLUE);
			return;
		}

		if (color == SiegeColor.BLUE)
			scoreboard.setBlueTowerHealth(ChatColor.translateAlternateColorCodes('&',
					"&9Health: &a" + (t.getHealth() >= 0 ? t.getHealth() : 0) + " &4❤"));
		else
			scoreboard.setRedTowerHealth(ChatColor.translateAlternateColorCodes('&',
					"&4Health: &a" + (t.getHealth() >= 0 ? t.getHealth() : 0) + " &4❤"));

	}

	@Override
	public void onGameStart(Tower blue, Tower red) {
		playing = true;

		onTowerDamage(SiegeColor.BLUE, blue);
		onTowerDamage(SiegeColor.RED, red);

		onPowerupsChange(SiegeColor.BLUE, blue);
		onPowerupsChange(SiegeColor.RED, red);

		redTeam.forEach(e -> SiegeItems.setEquipment(e));
		blueTeam.forEach(e -> SiegeItems.setEquipment(e));

		// default scoreboard
		scoreboard.enable();

	}

	@Override
	public void onPowerupsChange(SiegeColor color, Tower t) {
		if (color == SiegeColor.BLUE)
			scoreboard.setBlueTowerPowerups(
					ChatColor.translateAlternateColorCodes('&', "&9Powerups: &6" + t.getPowerups() + " &a♦"));
		else
			scoreboard.setRedTowerPowerups(
					ChatColor.translateAlternateColorCodes('&', "&4Powerups: &6" + t.getPowerups() + " &a♦"));
	}

	@Override
	public void onPowerupSpawn(Item item) {
		Bukkit.getOnlinePlayers().forEach(e -> e.sendMessage(ChatColor.GREEN + "New powerup has spawned!"));
	}

}
