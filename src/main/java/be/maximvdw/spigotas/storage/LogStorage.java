package be.maximvdw.spigotas.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import be.maximvdw.spigotas.ui.Console;

public class LogStorage {
	private String logName = ""; // Configuration name
	String folder = ""; // Folder

	/**
	 * Initialize the log storage
	 * 
	 * @param plugin
	 *            Plugin
	 */
	public LogStorage(String logName) {
		this.logName = logName;
		loadConfig();
	}

	/**
	 * Initialize the log storage
	 * 
	 * @param plugin
	 *            Plugin
	 */
	public LogStorage(String folder, String logName) {
		this.folder = folder;
		this.logName = logName;
		loadConfig();
	}

	// Yaml Configuration
	private File logFile;

	/**
	 * Get file contents
	 * 
	 * @return Contents
	 */
	public String getContents() {
		try {
			FileInputStream fis = new FileInputStream(logFile);
			byte[] data = new byte[(int) logFile.length()];
			fis.read(data);
			fis.close();
			//
			String s = new String(data, "UTF-8");
			return s;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Gets the log file.
	 * 
	 * @return Log file
	 */
	public File getLogFile() {
		return logFile;
	}

	/**
	 * Append to log file
	 * 
	 * @param message
	 *            Message
	 */
	public void appendLog(final String message) {
		try {
			FileWriter fw = new FileWriter(logFile, true);
			fw.write(message + "\n");
			fw.close();
		} catch (Exception ex) {

		}
	}

	/**
	 * Loads the configuration file
	 */
	public void loadConfig() {
		logFile = new File(logName + ".log");
		if (logFile.exists()) {

		} else {
			try {
				new File(folder).mkdir();
				Console.info("Creating new file '" + logFile + "'!");
				logFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
