package lifeFormTypes;

import artificialLifeSimulator.AWorld;
import artificialLifeSimulator.AWorldObject;
import artificialLifeSimulator.Direction;
import worldObjects.ALifeForm;
import worldObjects.AnObstacle;

/**
 * A type of LifeForm which will only eat herbivores within it's world.
 * 
 * @author Jed Brennen
 * @see ALifeForm
 * @see AWorldObject
 * 
 *
 */
public class ACarnivore extends ALifeForm {

	private static final long serialVersionUID = -8356865056285736130L;

	/**
	 * A type of LifeForm which will only eat herbivores within it's world.
	 * 
	 * @param world
	 *            - the world in which the carnivore is placed.
	 * @param name
	 *            - the name of the carnivore.
	 * @param energy
	 *            - the initial energy of the carnivore.
	 * @param id
	 *            - the id number of the carnivore.
	 */
	public ACarnivore(AWorld world, String name, int energy, int id) {
		super(world, name, "Carnivore", "file:resources/images/carnivore.png",
				(int) (Math.random() * world.getHeight()), (int) (Math.random() * world.getWidth()), energy, 2, id);
		try {
			initialLifeForm = (ALifeForm) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public Direction sense() {
		AWorldObject[][] map = world.getMap();
		Direction d = Direction.NORTH;
		AHerbivore food;
		int bestFood = 0;
		int senseDistance = 0;
		if (sensingDist > energy) {
			senseDistance = energy;
		} else {
			senseDistance = sensingDist;
		}

		int obstructionCount = 0;

		/* Scans for food up to the max sensing distance for the bug */
		for (int i = senseDistance; i > 0; i--) {
			/* Checks the y axis for food */
			// Makes sure not to scan outside the map range
			if ((y + i) < world.getHeight()) {
				/*
				 * Checks whether the position contains food, whether it's
				 * better than the current food found and whether there is an
				 * obstacle between the LifeForm and the food.
				 */
				if (map[y + i][x] instanceof AHerbivore && !(map[y + 1][x] instanceof AnObstacle)) {
					food = (AHerbivore) map[y + i][x];
					if (food.getEnergy() >= bestFood) {
						bestFood = food.getEnergy();
						d = Direction.SOUTH;
					}
				}

				if (i == 1) {
					if (map[y + i][x] != null && !(map[y + i][x] instanceof AHerbivore)) {
						obstructionCount++;
					}
				}

			}
			if ((y - i) >= 0) {

				if (map[y - i][x] instanceof AHerbivore && !(map[y - 1][x] instanceof AnObstacle)) {
					food = (AHerbivore) map[y - i][x];
					if (food.getEnergy() >= bestFood) {
						bestFood = food.getEnergy();
						d = Direction.SOUTH;
					}
				}

				if (i == 1) {
					if (map[y - i][x] != null && !(map[y - i][x] instanceof AHerbivore)) {
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
				if (map[y][x + i] instanceof AHerbivore && !(map[y][x + 1] instanceof AnObstacle)) {
					food = (AHerbivore) map[y][x + i];
					if (food.getEnergy() >= bestFood) {
						bestFood = food.getEnergy();
						d = Direction.EAST;
					}
				}

				if (i == 1) {
					if (map[y][x + i] != null && !(map[y][x + i] instanceof AHerbivore)) {
						obstructionCount++;
					}
				}

			}

			if ((x - i) >= 0) {

				if (map[y][x - i] instanceof AHerbivore && !(map[y][x - 1] instanceof AnObstacle)) {
					food = (AHerbivore) map[y][x - i];
					if (food.getEnergy() >= bestFood) {
						bestFood = food.getEnergy();
						d = Direction.WEST;
					}
				}

				if (i == 1) {
					if (map[y][x - i] != null && !(map[y][x - i] instanceof AHerbivore)) {
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
				if (object != null && !(object instanceof AHerbivore)) {
					return object;
				} else {
					if (object instanceof AHerbivore) {
						((AHerbivore) object).setAlive(false);
					}
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
				if (object != null && !(object instanceof AHerbivore)) {
					return object;
				} else {
					if (object instanceof AHerbivore) {
						((AHerbivore) object).setAlive(false);
					}
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
				if (object != null && !(object instanceof AHerbivore)) {
					return object;
				} else {
					if (object instanceof AHerbivore) {
						((AHerbivore) object).setAlive(false);
					}
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
				if (object != null && !(object instanceof AHerbivore)) {
					return object;
				} else {
					if (object instanceof AHerbivore) {
						((AHerbivore) object).setAlive(false);
					}
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
