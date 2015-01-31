package be.maximvdw.spigotas.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlStorage {
	private String configName = ""; // Configuration name
	private String folder = ""; // Folder
	private Yaml yaml = new Yaml();
	private Map<String, Object> result = new HashMap<String, Object>();

	/**
	 * Initialize the Yaml Configuration
	 * 
	 * @param plugin
	 *            Plugin
	 */
	public YamlStorage(String configName) {
		this.configName = configName;
		loadConfig();
	}

	/**
	 * Initialize the Yaml Configuration
	 * 
	 * @param plugin
	 *            Plugin
	 */
	public YamlStorage(String folder, String configName) {
		this.folder = folder;
		this.configName = configName;
		loadConfig();
	}

	// Yaml Configuration
	private File configFile;

	/**
	 * Gets the configuration file.
	 * 
	 * @return Configuration file
	 */
	public File getConfigFile() {
		return configFile;
	}

	/**
	 * Loads the configuration file
	 */
	@SuppressWarnings("unchecked")
	public void loadConfig() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
		configFile = new File(folder, configName + ".yml");
		if (!configFile.exists()) {
			// Create file
			try {
				new File(folder).mkdirs();
				configFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (configFile.exists()) {
			// Load the configuration file
			InputStream ios = null;
			try {
				ios = new FileInputStream(configFile);
				result = (Map<String, Object>) yaml.load(ios);
			} catch (Exception e) {
				// Error while loading the file
			}
			if (result == null)
				result = new HashMap<String, Object>();
			try {
				if (ios != null)
					ios.close();
			} catch (Exception ex) {

			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object get(String path) {
		String[] pathArr = path.split("\\.");
		if (pathArr.length == 0) {
			pathArr = new String[1];
			pathArr[0] = path;
		}
		Object lastObj = result;
		for (int i = 0; i < pathArr.length; i++) {
			lastObj = ((Map<String, Object>) lastObj).get(pathArr[i]);
		}
		return lastObj;
	}

	/**
	 * Get boolean value from configuration
	 * 
	 * @param path
	 *            Configuration path
	 * @return Boolean value
	 */
	public boolean getBoolean(String path) {
		Object lastObj = get(path);
		if (lastObj instanceof Boolean)
			return (Boolean) lastObj;
		return false;
	}

	/**
	 * Get integer value from configuration
	 * 
	 * @param path
	 *            Configuration path
	 * @return Integer result
	 */
	public int getInt(String path) {
		Object lastObj = get(path);
		if (lastObj instanceof Integer)
			return (Integer) lastObj;
		return 0;
	}

	/**
	 * Get string value from configuration
	 * 
	 * @param path
	 *            Configuration path
	 * @return String result
	 */
	public String getString(String path) {
		Object lastObj = get(path);
		if (lastObj instanceof String)
			return (String) lastObj;
		return "";
	}

	@SuppressWarnings("unchecked")
	public void set(String path, Object value) {
		String[] pathArr = path.split("\\.");
		if (pathArr.length == 0) {
			pathArr = new String[1];
			pathArr[0] = path;
		}
		Map<String, Object> lastObj = result;
		for (int i = 0; i < pathArr.length; i++) {
			String pathPart = pathArr[i];
			boolean isEnd = i >= pathArr.length - 1;
			if (isEnd) {
				lastObj.put(pathPart, value);
			} else {
				if (!lastObj.containsKey(pathPart)) {
					lastObj.put(pathPart, new HashMap<String, Object>());
				}
				lastObj = (Map<String, Object>) lastObj.get(pathPart);
			}
		}
	}

	/**
	 * Save the yaml file
	 */
	public void save() {
		try {
			StringWriter writer = new StringWriter();
			yaml.dump(result, writer);
			// Dump the YAML file
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(
					getConfigFile()));
			bwriter.write(writer.toString());

			// Close writer
			bwriter.close();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
