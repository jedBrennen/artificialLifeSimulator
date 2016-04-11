package worldObjects;

import java.io.Serializable;

import artificialLifeSimulator.AWorld;
import artificialLifeSimulator.AWorldObject;
import artificialLifeSimulator.Direction;

/**
 * A world object which can move and sense other objects within the world. It
 * will search for food and die when it runs out of energy.
 * 
 * @author Jed Brennen
 * @see AWorldObject
 * 
 *
 */
public abstract class ALifeForm extends AWorldObject implements Serializable {

	private static final long serialVersionUID = 463846516348454280L;

	protected int sensingDist;
	protected String species, name;
	protected int energy, id;
	protected int poison;
	protected boolean alive;
	protected ALifeForm initialLifeForm;

	/**
	 * A world object which can move and sense other objects within the world.
	 * It will search for food and die when it runs out of energy.
	 * 
	 * @param world
	 *            - the world in which the LifeForm is placed.
	 * @param name
	 *            - the name of the LifeForm.
	 * @param species
	 *            - the species of the LifeForm.
	 * @param symbol
	 *            - the path of the LifeForm's image representation.
	 * @param y
	 *            - the y coordinate of the LifeForm.
	 * @param x
	 *            - the x coordinate of the LifeForm.
	 * @param energy
	 *            - the energy of the LifeForm.
	 * @param sensingDistance
	 *            - the distance over which the LifeForm can sense food.
	 * @param id
	 *            - the id number of the LifeForm.
	 */
	public ALifeForm(AWorld world, String name, String species, String symbol, int y, int x, int energy,
			int sensingDistance, int id) {
		super(world, symbol);
		this.name = name;
		this.species = species;
		this.y = y;
		this.x = x;
		this.energy = energy;
		this.sensingDist = sensingDistance;
		this.id = id;
		poison = 0;
		if (energy > 0) {
			alive = true;
		}
	}

	/**
	 * Senses the type of food eaten by the particular life form by searching
	 * horizontally and vertically on the x and y axis' over the range of the
	 * LifeForm's senseDistance. The method will look for the food with the
	 * greatest value and return the direction of the food. If no food is found
	 * this method will return a randomly selected direction.
	 * 
	 * @return - Direction of food with greatest value or random direction.
	 * @see Direction
	 */
	public abstract Direction sense();

	/**
	 * This method moves the LifeForm 1 space in the specified direction. It
	 * will return whatever world object previously occupied the space when the
	 * LifeForm moved. If the space to which the LifeForm moved is empty the
	 * method will return null. The method will also return null if the LifeForm
	 * is unable to move due to obstruction.
	 * 
	 * @param d
	 *            - the direction in which to move.
	 * @return the world object in the square which has been moved to.
	 */
	public abstract AWorldObject move(Direction d);

	public String toString() {
		String text = "";
		text += "(" + id + ") " + name + " @ " + (x + 1) + ", " + (y + 1) + ". ";

		return text;
	}

	/**
	 * @return a string representation of the species of the LifeForm.
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @return the name of the LifeForm.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the energy level of the LifeForm.
	 */
	public int getEnergy() {
		return energy;
	}

	/**
	 * @return the id value of the LifeForm.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Returns the distance over which the LifeForm can search for food.
	 * 
	 * @return the sensing distance of the LifeForm.
	 */
	public int getSensingDistance() {
		return sensingDist;
	}

	/**
	 * Generates and sets new, random, coordinates for the LifeForm.
	 */
	public void getNewPosition() {
		boolean placed = false;
		do {
			int y = (int) Math.round(Math.random() * (world.getHeight() - 1));
			int x = (int) Math.round(Math.random() * (world.getWidth() - 1));
			if (!(world.getMap()[y][x] instanceof ALifeForm)) {
				this.x = x;
				this.y = y;
				placed = true;
			}
		} while (!placed);
	}

	/**
	 * Returns the initial LifeForm object, used to store initial values of the
	 * LifeForm either when it was created or reset.
	 * 
	 * @return returns the initial LifeForm object.
	 */
	public ALifeForm getInitialLifeForm() {
		return initialLifeForm;
	}

	/**
	 * @return the poison value of the LifeForm.
	 */
	public int getPoison() {
		return poison;
	}

	/**
	 * @return true if the LifeForm has an energy greater than 0.
	 */
	public boolean isAlive() {
		if (energy <= 0) {
			alive = false;
		} else {
			alive = true;
		}
		return alive;
	}

	/**
	 * @param newEnergy
	 *            - the new energy value for the LifeForm.
	 */
	public void setEnergy(int newEnergy) {
		energy = newEnergy;
	}

	/**
	 * @param newId
	 *            - the new ID value for the LifeForm.
	 */
	public void setId(int newId) {
		id = newId;
	}

	/**
	 * @param poison
	 *            - the new poison value for the LifeForm.
	 */
	public void setPoison(int poison) {
		this.poison = poison;
	}

	/**
	 * @param alive
	 *            - Set to true if LifeForm is alive, false if dead.
	 */
	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	/**
	 * Resets the initial LifeForm object, resetting all initial values with it.
	 */
	public void resetInitialLifeForm() {
		try {
			initialLifeForm = (ALifeForm) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

}
