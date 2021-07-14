package com.siege.data.constants;

import org.bukkit.ChatColor;

/**
 * Siege class used to indicate team colors
 * 
 * @author Tommaso
 *
 */
public enum SiegeColor {

	BLUE, RED;

	public String toString() {
		return this == BLUE ? ChatColor.BLUE + "Blue" : ChatColor.DARK_RED + "Red";
	}

}
