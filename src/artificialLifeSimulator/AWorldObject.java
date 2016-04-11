package artificialLifeSimulator;

import java.io.Serializable;

/**
 * An object which is contained within the world and displayed on the map during
 * the simulation.
 * 
 * @author Jed Brennen
 */
public abstract class AWorldObject implements Serializable, Cloneable {

	private static final long serialVersionUID = 5099776872991181442L;

	protected final AWorld world;
	protected final String symbol;
	protected int x, y;
	protected int timer;

	/**
	 * 
	 * @param world
	 *            - the world within which the object is placed.
	 * @param symbol
	 *            - the symbol that represents the object on the map.
	 */
	public AWorldObject(AWorld world, String symbol) {
		this.world = world;
		this.symbol = symbol;
		timer = 0;
	}

	/**
	 * @return the world within which the object is placed.
	 */
	public AWorld getWorld() {
		return world;
	}

	/**
	 * @return the symbol which represents the image on the map.
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * This returns the column index (x coordinate) of the object in the map
	 * array.
	 * 
	 * @return the x coordinate of the object.
	 */
	public int getX() {
		return x;
	}

	/**
	 * This returns the row index (y coordinate) of the object in the map array.
	 * 
	 * @return the y coordinate of the object.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Returns the timer which represents the time until an object can
	 * move/respawn etc.
	 * 
	 * @return the object's timer
	 */
	public int getTimer() {
		return timer;
	}

	/**
	 * Sets the column index (x coordinate) of the object in the map array.
	 * 
	 * @param x
	 *            - the value to set for the object's x coordinate.
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Sets the row index (y coordinate) of the object in the map array.
	 * 
	 * @param y
	 *            - the value to set for the object's y coordinate.
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @param cycles
	 *            - the value to set for the timer.
	 */
	public void setTimer(int cycles) {
		timer = cycles;
	}

	/**
	 * Reduces the object's timer by 1.
	 */
	public void decrementTimer() {
		timer--;
	}
}
