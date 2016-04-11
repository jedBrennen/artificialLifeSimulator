package lifeFormTypes;

import artificialLifeSimulator.AWorld;
import artificialLifeSimulator.AWorldObject;
import artificialLifeSimulator.Direction;
import worldObjects.AFood;
import worldObjects.ALifeForm;
import worldObjects.AnObstacle;

/**
 * AHerbivore is a type of LifeForm which will only eat food objects within it's
 * world.
 * 
 * @author Jed Brennen
 * @see ALifeForm
 * @see AWorldObject
 * 
 *
 */
public class AHerbivore extends ALifeForm {

	private static final long serialVersionUID = 1823836058046673406L;

	/**
	 * AHerbivore is a type of LifeForm which will only eat food objects within
	 * it's world.
	 * 
	 * @param world
	 *            - the world in which the herbivore is placed.
	 * @param name
	 *            - the name of the herbivore.
	 * @param energy
	 *            - the initial energy of the herbivore.
	 * @param id
	 *            - the id number of the herbivore.
	 */
	public AHerbivore(AWorld world, String name, int energy, int id) {
		super(world, name, "Herbivore", "file:resources/images/herbivore.png",
				(int) (Math.random() * world.getHeight()), (int) (Math.random() * world.getWidth()), energy, 2, id);
		try {
			initialLifeForm = (ALifeForm) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public Direction sense() {
		AWorldObject[][] map = world.getMap();
		Direction d = null;
		AFood food;
		int bestFood = 0;
		int sensingDist = 0;
		if (this.sensingDist > energy) {
			sensingDist = energy;
		} else {
			sensingDist = this.sensingDist;
		}

		int obstructionCount = 0;

		/* Scans for food up to the max sensing distance for the bug */
		for (int i = sensingDist; i > 0; i--) {
			// Checks the y axis for food
			// Makes sure not to scan outside the map range
			if ((y + i) < world.getHeight()) {
				/*
				 * Checks whether the position contains food, whether it's
				 * better than the current food found and whether there is an
				 * obstacle between the LifeForm and the food.
				 */
				if (map[y + i][x] instanceof AFood && !(map[y + 1][x] instanceof AnObstacle)) {
					food = (AFood) map[y + i][x];
					if (food.getValue() >= bestFood) {
						bestFood = food.getValue();
						d = Direction.SOUTH;
					}
				}

				if (i == 1) {
					if (map[y + i][x] != null && !(map[y + i][x] instanceof AFood)) {
						obstructionCount++;
					}
				}

			}
			if ((y - i) >= 0) {

				if (map[y - i][x] instanceof AFood && !(map[y - 1][x] instanceof AnObstacle)) {
					food = (AFood) map[y - i][x];
					if (food.getValue() >= bestFood) {
						bestFood = food.getValue();
						d = Direction.NORTH;
					}
				}

				if (i == 1) {
					if (map[y - i][x] != null && !(map[y - i][x] instanceof AFood)) {
						obstructionCount++;
					}
				}

			}
			/* Checks the x axis for food */
			// Makes sure not to scan outside the map range
			if ((x + i) < world.getWidth()) {
				/*
				 * Checks whether the position contains food, whether it's
				 * better than the current food found and whether there is an
				 * obstacle between the LifeForm and the food.
				 */
				if (map[y][x + i] instanceof AFood && !(map[y][x + 1] instanceof AnObstacle)) {
					food = (AFood) map[y][x + i];
					if (food.getValue() >= bestFood) {
						bestFood = food.getValue();
						d = Direction.EAST;
					}
				}

				if (i == 1) {
					if (map[y][x + i] != null && !(map[y][x + i] instanceof AFood)) {
						obstructionCount++;
					}
				}

			}

			if ((x - i) >= 0) {

				if (map[y][x - i] instanceof AFood && !(map[y][x - 1] instanceof AnObstacle)) {
					food = (AFood) map[y][x - i];
					if (food.getValue() >= bestFood) {
						bestFood = food.getValue();
						d = Direction.WEST;
					}
				}

				if (i == 1) {
					if (map[y][x - i] != null && !(map[y][x - i] instanceof AFood)) {
						obstructionCount++;
					}
				}

			}
		}
		// Used if no food is found to move bug randomly
		if (bestFood == 0) {
			if (obstructionCount < 4) {
				double num = Math.random();
				// Uses a random number to decide in which direction to move the
				// bug
				if (num >= 0 && num < 0.25) {
					if (y > 0) {
						d = Direction.NORTH;
					} else {
						d = Direction.SOUTH;
					}
				}
				if (num >= 0.25 && num < 0.5) {
					if (x < world.getWidth() - 1) {
						d = Direction.EAST;
					} else {
						d = Direction.WEST;
					}
				}
				if (num >= 0.5 && num < 0.75) {
					if (y < world.getHeight() - 1) {
						d = Direction.SOUTH;
					} else {
						d = Direction.NORTH;
					}
				}
				if (num >= 0.75 && num < 1) {
					if (x > 0) {
						d = Direction.WEST;
					} else {
						d = Direction.EAST;
					}
				}
			} else {
				return null;
			}
		}
		return d;
	}

	public AWorldObject move(Direction d) {
		AWorldObject[][] map = world.getMap();
		AWorldObject object;
		switch (d) {
		case NORTH:
			if ((y - 1) >= 0) {
				object = map[y - 1][x];
				if (object != null && !(object instanceof AFood)) {
					return object;
				} else {
					world.setMapValue(null, y, x);
					y--;
					world.setMapValue(this, y, x);
					return object;
				}
			} else {
				return new AnObstacle(world);
			}

		case EAST:
			if ((x + 1) < world.getWidth()) {
				object = map[y][x + 1];
				if (object != null && !(object instanceof AFood)) {
					return object;
				} else {
					world.setMapValue(null, y, x);
					x++;
					world.setMapValue(this, y, x);
					return object;
				}
			} else {
				return new AnObstacle(world);
			}

		case SOUTH:
			if ((y + 1) < world.getHeight()) {
				object = map[y + 1][x];
				if (object != null && !(object instanceof AFood)) {
					return object;
				} else {
					world.setMapValue(null, y, x);
					y++;
					world.setMapValue(this, y, x);
					return object;
				}
			} else {
				return new AnObstacle(world);
			}

		case WEST:
			if ((x - 1) >= 0) {
				object = map[y][x - 1];
				if (object != null && !(object instanceof AFood)) {
					return object;
				} else {
					world.setMapValue(null, y, x);
					x--;
					world.setMapValue(this, y, x);
					return object;
				}
			} else {
				return new AnObstacle(world);
			}

		default:
			return new AnObstacle(world);
		}
	}
}
