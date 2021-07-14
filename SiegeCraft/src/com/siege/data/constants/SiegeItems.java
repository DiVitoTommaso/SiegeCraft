package com.siege.data.constants;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SiegeItems {

	private static final ItemStack POWERUP = new ItemStack(Material.EMERALD);
	private static final ItemStack TNT = new ItemStack(Material.TNT);

	private static final ItemStack HELMET = new ItemStack(Material.DIAMOND_HELMET);
	private static final ItemStack CHESTPLATE = new ItemStack(Material.DIAMOND_CHESTPLATE);
	private static final ItemStack LEGGINS = new ItemStack(Material.DIAMOND_LEGGINGS);
	private static final ItemStack BOOTS = new ItemStack(Material.DIAMOND_BOOTS);

	private static final ItemStack SWORD = new ItemStack(Material.DIAMOND_SWORD);
	private static final ItemStack BOW = new ItemStack(Material.BOW);
	private static final ItemStack ARROW = new ItemStack(Material.ARROW);
	private static final ItemStack PICKAXE = new ItemStack(Material.DIAMOND_PICKAXE);

	private static final ItemStack BLOCKS = new ItemStack(Material.SANDSTONE);
	private static final ItemStack STEAKS = new ItemStack(Material.COOKED_BEEF);

	static {

		BLOCKS.setAmount(64);
		STEAKS.setAmount(64);

		POWERUP.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

		TNT.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		TNT.setAmount(10);

		HELMET.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
		CHESTPLATE.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
		LEGGINS.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
		BOOTS.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);

		HELMET.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
		CHESTPLATE.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
		LEGGINS.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
		BOOTS.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);

		HELMET.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
		CHESTPLATE.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
		LEGGINS.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
		BOOTS.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		BOW.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		BOW.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 5);
		BOW.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);

		SWORD.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);

		PICKAXE.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);

		// not working unbreakable flag. Ehm? i don't know this api makes you rage quit
		HELMET.getItemMeta().setUnbreakable(true);
		HELMET.setItemMeta(HELMET.getItemMeta());

		CHESTPLATE.getItemMeta().setUnbreakable(true);
		CHESTPLATE.setItemMeta(CHESTPLATE.getItemMeta());

		LEGGINS.getItemMeta().setUnbreakable(true);
		LEGGINS.setItemMeta(LEGGINS.getItemMeta());

		BOOTS.getItemMeta().setUnbreakable(true);
		BOOTS.setItemMeta(BOOTS.getItemMeta());

		BOW.getItemMeta().setUnbreakable(true);
		BOW.setItemMeta(BOW.getItemMeta());

		SWORD.getItemMeta().setUnbreakable(true);
		SWORD.setItemMeta(SWORD.getItemMeta());

		PICKAXE.getItemMeta().setUnbreakable(true);
		PICKAXE.setItemMeta(PICKAXE.getItemMeta());
	}

	private SiegeItems() {
		throw new IllegalAccessError("Cannot instantiate this class");
	}

	/**
	 * get a clone of the powerup game item
	 * 
	 * @return EMERALD clone
	 */

	public static ItemStack POWERUP() {
		return POWERUP.clone();
	}

	/**
	 * get a clone of the bomb game item
	 * 
	 * @return TNT clone
	 */

	public static ItemStack BOMB() {
		return TNT.clone();
	}

	/**
	 * give to a player the basic equipment to battle
	 * 
	 * @param e
	 */

	public static void setEquipment(Player e) {
		e.getInventory().clear();

		e.getEquipment().setHelmet(HELMET.clone());
		e.getEquipment().setChestplate(CHESTPLATE.clone());
		e.getEquipment().setLeggings(LEGGINS.clone());
		e.getEquipment().setBoots(BOOTS.clone());

		e.getInventory().addItem(SWORD.clone(), BOW.clone(), ARROW.clone(), PICKAXE.clone(), BLOCKS.clone(),
				STEAKS.clone());
	}
}
