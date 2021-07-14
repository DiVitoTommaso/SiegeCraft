package siege.util;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * craete a immutable location. Useful for mapping location to blocks and
 * retrieve them
 * 
 * @author Tommaso
 *
 */
public final class ImmutableLocation {

	private final Location l;

	public ImmutableLocation(Location l) {
		this.l = l;
	}

	/**
	 * get world associated to this lcoation
	 * 
	 * @return
	 */

	public World getWorld() {
		return l.getWorld();
	}

	/**
	 * get x associated to this lcoation
	 * 
	 * @return
	 */

	public double getX() {
		return l.getX();
	}

	/**
	 * get y associated to this lcoation
	 * 
	 * @return
	 */

	public double getY() {
		return l.getY();
	}

	/**
	 * get z associated to this lcoation
	 * 
	 * @return
	 */

	public double getZ() {
		return l.getZ();
	}

	/**
	 * get block associated to this lcoation
	 * 
	 * @return
	 */

	public Block getBlock() {
		return l.getBlock();
	}

	/**
	 * check if two ImmutableLocation are equal
	 */
	public boolean equals(Object o2) {
		if (!(o2 instanceof ImmutableLocation))
			return false;

		ImmutableLocation tmp = (ImmutableLocation) o2;
		return tmp.getX() == getX() && tmp.getY() == getY() && tmp.getZ() == getZ() && tmp.getWorld() == getWorld();
	}

	/**
	 * get the hashcode for this object
	 */
	public int hashCode() {
		return Objects.hash(getX(), getY(), getZ());
	}

}
