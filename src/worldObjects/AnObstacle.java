package worldObjects;

import java.io.Serializable;

import artificialLifeSimulator.AWorld;
import artificialLifeSimulator.AWorldObject;

/**
 * An obstacle is a world object which remains unmoved and unchanged throughout
 * the simulation. It will block LifeForms from moving and sensing food.
 * 
 * @author Jed Brennen
 * @see AWorldObject
 * 
 *
 */
public class AnObstacle extends AWorldObject implements Serializable {

	private static final long serialVersionUID = -4892388459136821070L;

	/**
	 * An obstacle is a world object which remains unmoved and unchanged
	 * throughout the simulation. It will block LifeForms from moving and
	 * sensing food.
	 * 
	 * @param world
	 *            - the world within which the obstacle is placed.
	 */
	public AnObstacle(AWorld world) {
		super(world, "file:resources/images/obstacle.png");
	}
}
