package artificialLifeSimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Handler for the configuration file. Performs all operations to do with
 * saving, loading, reading and writing to and from the world and configuration
 * files.
 * 
 * @author Jed Brennen
 *
 */
public class Configuration {

	private File configFile;
	private File lastConfig;
	private String filePath;
	private Properties p;

	/**
	 * Handler for the configuration file. Performs all operations to do with
	 * saving, loading, reading and writing to and from the world and
	 * configuration files.
	 */
	public Configuration() {
		lastConfig = new File("lastConfig.txt");
		if (lastConfig.exists() && new File(getLastConfigName()).exists()) {

			filePath = getLastConfigName();

		} else {

			filePath = "default.xml";
			setLastConfigName(filePath);

		}

		configFile = new File(filePath);
		p = new Properties();

		if (!configFile.exists()) {
			p.setProperty("mapWidth", "0");
			p.setProperty("mapHeight", "0");
			p.setProperty("foodDensity", "0");
			p.setProperty("objDensity", "0");
			p.setProperty("cycles", "0");
			p.setProperty("dispalyIterations", "true");

			// Store config file
			try {
				FileOutputStream fos = new FileOutputStream(configFile);
				p.storeToXML(fos, "");
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				FileInputStream fis = new FileInputStream(filePath);
				p.loadFromXML(fis);
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param property
	 * @return
	 */
	public String getProperty(String property) {
		return p.getProperty(property);
	}

	/**
	 * @param property
	 * @param value
	 */
	public void setProperty(String property, String value) {
		p.setProperty(property, value);
	}

	/**
	 * 
	 */
	public void save() {
		// Save config file
		try {
			p.storeToXML(new FileOutputStream(configFile), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param newFile
	 */
	public void saveAs(File newFile) {
		try {
			p.storeToXML(new FileOutputStream(newFile), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		configFile = newFile;
		filePath = newFile.getAbsolutePath();
		setLastConfigName(filePath);
	}

	/**
	 * @param filePath
	 */
	public void load(String filePath) {
		String oldFilePath = this.filePath;
		File oldFile = configFile;
		this.filePath = filePath;
		configFile = new File(filePath);
		setLastConfigName(filePath);
		try {
			FileInputStream fis = new FileInputStream(filePath);
			p.loadFromXML(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.filePath = oldFilePath;
			configFile = oldFile;
			setLastConfigName(oldFilePath);
		}
	}

	/**
	 * @param newFile
	 */
	public void create(File newFile) {
		configFile = newFile;
		p = new Properties();

		p.setProperty("mapWidth", "0");
		p.setProperty("mapHeight", "0");
		p.setProperty("foodDensity", "0");
		p.setProperty("objDensity", "0");
		p.setProperty("cycles", "0");
		p.setProperty("displayIterations", "true");

		// Store config file
		try {
			p.storeToXML(new FileOutputStream(configFile), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.filePath = newFile.getAbsolutePath();
		setLastConfigName(filePath);
	}

	/**
	 * @param newName
	 * @return
	 */
	public Path setName(String newName) {
		if (!newName.equalsIgnoreCase("")) {
			String parentPath = configFile.getAbsoluteFile().getParentFile().getAbsolutePath() + "\\";
			Path path = configFile.toPath();
			Path worldPath = new File(parentPath + getFileName(false) + ".world").toPath();
			Path newPath = configFile.toPath();
			try {
				newPath = Files.move(path, path.resolveSibling(parentPath + newName + ".xml"),
						StandardCopyOption.REPLACE_EXISTING);
				Files.move(worldPath, worldPath.resolveSibling(parentPath + newName + ".world"),
						StandardCopyOption.REPLACE_EXISTING);
				filePath = newPath.toString();
				configFile = new File(filePath);
				setLastConfigName(filePath);
				return newPath;
			} catch (IOException e) {
				e.printStackTrace();
				if (newPath.getFileName() != path.getFileName()) {
					try {
						Files.move(newPath, newPath.resolveSibling(path), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * @param withExtension
	 * @return
	 */
	public String getFileName(boolean withExtension) {
		if (withExtension) {
			return configFile.getName();
		} else {
			String name = configFile.getName();
			return name.substring(0, name.lastIndexOf('.'));
		}
	}

	/**
	 * @param withExtension
	 * @return
	 */
	public String getFilePath(boolean withExtension) {
		if (withExtension) {
			return configFile.getAbsolutePath();
		} else {
			String name = configFile.getAbsolutePath();
			return name.substring(0, name.lastIndexOf('.'));
		}
	}

	/**
	 * @return
	 */
	public File getFile() {
		return configFile;
	}

	/**
	 * @return
	 */
	public String getLastConfigName() {
		File f = new File("lastConfig.txt");
		String filePath = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			filePath = br.readLine();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filePath;
	}

	/**
	 * @param configName
	 */
	public void setLastConfigName(String configName) {
		try {
			FileWriter fw = new FileWriter("lastConfig.txt", false);
			fw.write(configName);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param filePath
	 */
	public void deleteFile(String filePath) {
		File f = new File(filePath);
		f.delete();
	}

	/**
	 * Returns the extension of a given file. If no extension is detected the
	 * last file in the filepath will be returned. Examples:
	 * 
	 * File with name "Document.txt" will return "txt". File with filepath
	 * "C:/User/example/Desktop/Document.xml" will return "xml". File with name
	 * "Document" will return "Document".
	 * 
	 * @param f
	 *            - the file from which to extract the extension.
	 * @return the extension of the file.
	 */
	public String getExtension(File f) {
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		int j = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > j) {
			return fileName.substring(i + 1);
		}

		return "";
	}
}
