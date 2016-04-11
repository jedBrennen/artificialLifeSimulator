package artificialLifeSimulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import worldObjects.AFood;
import worldObjects.ALifeForm;
import worldObjects.AnObstacle;

import java.io.Serializable;

/**
 * Class containing all objects contained within the world.
 * 
 * @author Jed Brennen
 *
 */
public class AWorld implements Serializable {

	private static final long serialVersionUID = 6889237664144514601L;

	private ArrayList<ALifeForm> lifeFormList = new ArrayList<>();
	private ArrayList<AWorldObject> foodList = new ArrayList<>();
	private ArrayList<AWorldObject> obstacleList = new ArrayList<>();
	private AWorldObject[][] map;
	private int width, height;
	private double foodDensity, objDensity;
	private transient Configuration config;

	/**
	 * Class containing all objects contained within the world.
	 */
	public AWorld() {
		config = new Configuration();
		width = Integer.parseInt(config.getProperty("mapWidth"));
		height = Integer.parseInt(config.getProperty("mapHeight"));
		foodDensity = Double.parseDouble(config.getProperty("foodDensity"));
		objDensity = Double.parseDouble(config.getProperty("objDensity"));
		map = new AWorldObject[height][width];
	}

	/**
	 * Class containing all objects contained within the world.
	 * 
	 * @param config
	 *            - configuration to load in to the world.
	 */
	public AWorld(Configuration config) {
		this.config = config;
		width = Integer.parseInt(config.getProperty("mapWidth"));
		height = Integer.parseInt(config.getProperty("mapHeight"));
		foodDensity = Double.parseDouble(config.getProperty("foodDensity"));
		objDensity = Double.parseDouble(config.getProperty("objDensity"));
		map = new AWorldObject[height][width];
	}

	/**
	 * Places the specified LifeForm in the map. The method will search for an
	 * empty map space to place the LifeForm in first before overwritting other
	 * non-LifeForm world objects.
	 * 
	 * @param lf
	 *            - LifeForm to place in the world.
	 */
	public void placeLifeForm(ALifeForm lf) {
		boolean placed;
		int y, x;

		placed = false;
		y = lf.getY();
		x = lf.getX();
		// Checks whether the map space the bug is being placed in is
		// available.
		if (y < height && x < width && !(map[y][x] instanceof AWorldObject)) {
			map[y][x] = lf;
			placed = true;
		} else {
			// Loops through the map checking whether there is an empty space
			// and whether the map is full i.e. full of LifeForms
			boolean positionAvailable = false;
			boolean mapFull = true;
			for (AWorldObject[] mapY : map) {
				for (AWorldObject mapX : mapY) {
					if (mapX == null) {
						positionAvailable = true;
					}
					if (!(mapX instanceof ALifeForm)) {
						mapFull = false;
					}
				}
			}

			if (!mapFull) {
				if (positionAvailable) {
					// Loops until LifeForm is placed in an empty space on the
					// map.
					do {
						y = (int) Math.round(Math.random() * (height - 1));
						x = (int) Math.round(Math.random() * (width - 1));
						if (!(map[y][x] instanceof AWorldObject)) {
							lf.setY(y);
							lf.setX(x);
							map[y][x] = lf;
							placed = true;
						}
					} while (!placed);
				} else {
					do {
						// Loops until a non-LifeForm map space is found and
						// removes
						// the non-LifeForm object.
						y = (int) Math.round(Math.random() * (height - 1));
						x = (int) Math.round(Math.random() * (width - 1));
						if (!(map[y][x] instanceof ALifeForm)) {
							if (map[y][x] instanceof AFood) {
								foodList.remove(map[y][x]);
							} else if (map[y][x] instanceof AnObstacle) {
								obstacleList.remove(map[y][x]);
							}
							lf.setY(y);
							lf.setX(x);
							map[y][x] = lf;
							placed = true;
						}
					} while (!placed);
				}
			}
		}

	}

	/**
	 * Places the food objects on the map based on the food density value
	 * specified by the user. Won't place food over LifeForms and decides
	 * whether or not to override other world objects. If reset is true or the
	 * food list is empty the list will be cleared and a new list of food
	 * objects will be created, otherwise the existing food objects in the list
	 * will be re-placed.
	 * 
	 * @param reset
	 *            - whether or not to reset the food list
	 */
	public void placeFood(boolean reset) {
		if (foodList.isEmpty() || reset) {
			foodList.clear();
			double rand;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					rand = Math.random();
					if (rand < foodDensity) {
						int value = (int) Math.round(Math.random() * 9);
						if (value == 0) {
							value = 1;
						}
						AWorldObject obstacle;
						AWorldObject f = new AFood(this, value, decide(3));
						if (map[y][x] == null) {
							f.setX(x);
							f.setY(y);
							map[y][x] = f;
							foodList.add(f);
						} else if (map[y][x] instanceof AnObstacle) {
							if (decide(2)) {
								obstacle = map[y][x];
								f.setX(x);
								f.setY(y);
								map[y][x] = f;
								obstacleList.remove(obstacle);
								foodList.add(f);
							}
						}
					}
				}
			}
		} else {
			for (AWorldObject f : foodList) {
				if (f.getTimer() == 0) {
					if (map[f.getY()][f.getX()] instanceof AnObstacle) {
						if (decide(2)) {
							obstacleList.remove(map[f.getY()][f.getX()]);
							map[f.getY()][f.getX()] = f;
						}
					} else if (map[f.getY()][f.getX()] == null) {
						map[f.getY()][f.getX()] = f;
					}
				}
			}
		}
	}

	/**
	 * Places the obstalces on the map based on the obstacle density value
	 * specified by the user. Won't place obstacles over LifeForms and decides
	 * whether or not to override other world objects. If reset is true or the
	 * obstacle list is empty the list will be cleared and a new list of
	 * obstacles will be created, otherwise the existing obstacles in the list
	 * will be re-placed.
	 * 
	 * @param reset
	 *            - whether or not to reset the obstacle list
	 * 
	 */
	public void placeObstacles(boolean reset) {
		if (obstacleList.isEmpty() || reset) {
			obstacleList.clear();
			double rand;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					rand = Math.random();
					if (rand < objDensity) {
						AWorldObject food;
						AWorldObject o = new AnObstacle(this);
						if (map[y][x] == null) {
							o.setX(x);
							o.setY(y);
							map[y][x] = o;
							obstacleList.add(o);
						} else if (map[y][x] instanceof AFood) {
							if (decide(2)) {
								food = map[y][x];
								o.setX(x);
								o.setY(y);
								map[y][x] = o;
								foodList.remove(food);
								obstacleList.add(o);
							}
						}

					}
				}
			}
		} else {
			for (AWorldObject o : obstacleList) {
				if (map[o.getY()][o.getX()] instanceof AFood) {
					if (decide(2)) {
						foodList.remove(map[o.getY()][o.getX()]);
						map[o.getY()][o.getX()] = o;
					}
				} else if (map[o.getY()][o.getX()] == null) {
					map[o.getY()][o.getX()] = o;
				}
			}
		}
	}

	/**
	 * Sets all values in the map array to null.
	 */
	public void clearMap() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = null;
			}
		}
	}

	/**
	 * Performs a decision based on a given denominator used to create a
	 * threshold by dividing 1 by the parameter. If a randomly generated number
	 * is lower than the threshold the method will return true.
	 * 
	 * @return true if random number is lower than 1 / denominator.
	 */
	public boolean decide(double denominator) {
		double rand = Math.random();
		double threshold = 1 / denominator;
		if (rand < threshold) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Saves the world by serialising it and writing it to a file, including all
	 * objects contained within the world.
	 */
	public void save() {
		try {
			String fileName = config.getFilePath(false);
			FileOutputStream fos = new FileOutputStream(fileName + ".world", false);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads values in to the world from a world file using the given
	 * configuration file.
	 * 
	 * @param config
	 *            - configuration file to use when loading the world.
	 */
	public void load(Configuration config) {
		File f = new File(config.getFilePath(false) + ".world");
		if (f.exists()) {
			try {
				String filePath = config.getFilePath(false);
				FileInputStream fis = new FileInputStream(filePath + ".world");
				ObjectInputStream ois = new ObjectInputStream(fis);
				AWorld tempWorld = (AWorld) ois.readObject();
				this.lifeFormList = tempWorld.lifeFormList;
				this.foodList = tempWorld.foodList;
				this.obstacleList = tempWorld.obstacleList;
				this.map = tempWorld.map;
				this.width = tempWorld.width;
				this.height = tempWorld.height;
				this.foodDensity = tempWorld.foodDensity;
				this.objDensity = tempWorld.objDensity;
				this.config = config;
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			this.config = config;
			save();
		}
	}

	/**
	 * Synchronises the world's attributes with the current values in the
	 * configuration.
	 */
	public void syncConfiguration() {
		width = Integer.parseInt(config.getProperty("mapWidth"));
		height = Integer.parseInt(config.getProperty("mapHeight"));
		foodDensity = Double.parseDouble(config.getProperty("foodDensity"));
		objDensity = Double.parseDouble(config.getProperty("objDensity"));
		map = new AWorldObject[height][width];
	}

	/**
	 * @return the width of the map.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height of the map.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns a 2-dimensional array of world objects which represents the world
	 * map.
	 * 
	 * @return the world's map array.
	 */
	public AWorldObject[][] getMap() {
		return map;
	}

	/**
	 * @return the world's list of LifeForm objects.
	 */
	public ArrayList<ALifeForm> getLifeFormList() {
		return lifeFormList;
	}

	/**
	 * @return the world's list of food objects.
	 */
	public ArrayList<AWorldObject> getFoodList() {
		return foodList;
	}

	/**
	 * @return the world's list of obstacle objects.
	 */
	public ArrayList<AWorldObject> getObstacleList() {
		return obstacleList;
	}

	/**
	 * @param object - the object to place in the map.
	 * @param y - the row index in which to place the object.
	 * @param x - the column index in which to place the object.
	 */
	public void setMapValue(AWorldObject object, int y, int x) {
		map[y][x] = null;
		map[y][x] = object;
	}

}
