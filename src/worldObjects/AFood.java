package worldObjects;

import java.io.Serializable;

import artificialLifeSimulator.AWorld;
import artificialLifeSimulator.AWorldObject;

/**
 * 
 * A world object which can be eaten by certain LifeForms to supply varying
 * values of energy. Food can be poisonous in which case it will cause the
 * LifeForm which eats it to loose an amount of energy, over time, equal to the
 * value of the food.
 * 
 * @author Jed Brennen
 * @see AWorldObject
 * 
 *
 */
public class AFood extends AWorldObject implements Serializable {

	private static final long serialVersionUID = -7256231966211800093L;

	private int value;
	private boolean poisoned;
	private AFood initialFood; // Used when the user rewinds the map

	/**
	 * A world object which can be eaten by certain LifeForms to supply varying
	 * values of energy. Food can be poisonous in which case it will cause the
	 * LifeForm which eats it to loose an amount of energy, over time, equal to
	 * the value of the food.
	 * 
	 * @param world
	 *            - the world within which the food is placed.
	 * @param value
	 *            - the amount of energy to supply or deduct from the LifeForm.
	 * @param poisoned
	 *            - if true the food will be poisonous.
	 *
	 */
	public AFood(AWorld world, int value, boolean poisoned) {
		super(world, getSymbol(poisoned));
		this.value = value;
		this.poisoned = poisoned;
		try {
			initialFood = (AFood) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used only in the constructor to determine the symbol to set for the food
	 * object.
	 * 
	 * @param poisoned
	 *            - if true the poisoned food image will be returned.
	 * @return the path of the image, which represents the food, as a string.
	 */
	private static String getSymbol(boolean poisoned) {
		if (poisoned) {
			return "file:resources/images/poisonedFood.png";
		} else {
			return "file:resources/images/food.png";
		}
	}

	/**
	 * @return the energy value of the food.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return true if the food is poisonous.
	 */
	public boolean isPoisonous() {
		return poisoned;
	}

	/**
	 * @return the initial food object when it was created
	 */
	public AFood getInitialFood() {
		return initialFood;
	}
}
