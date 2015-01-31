package be.maximvdw.spigotas.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Console Utilities
 * 
 * @version 1.0.0
 * @date 06/10/2014
 * @author EhB - SPGr3
 */
public class Console {
	/**
	 * Log an info message
	 * 
	 * @param message
	 *            Message
	 */
	public static void info(String message) {
		Date today;
		String output;
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
		today = new Date();
		output = formatter.format(today);
		message = "[" + output + "]" + " [INFO] " + message;
		System.out.println(message);
	}

	/**
	 * Log an severe message
	 * 
	 * @param message
	 *            Message
	 */
	public static void severe(String message) {
		Date today;
		String output;
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
		today = new Date();
		output = formatter.format(today);
		message = "[" + output + "]" + " [SEVERE] " + message;
		System.out.println(message);
	}

	/**
	 * Log an warning message
	 * 
	 * @param message
	 *            Message
	 */
	public static void warning(String message) {
		Date today;
		String output;
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
		today = new Date();
		output = formatter.format(today);
		message = "[" + output + "]" + " [WARNING] " + message;
		System.out.println(message);
	}

	/**
	 * Read line of console
	 * 
	 * @return String
	 */
	public static String readLine(boolean masked) {
		if (masked) {
			return new String(System.console().readPassword());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			return br.readLine();
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * Read line of console
	 * 
	 * @return String
	 */
	public static String readLine() {
		return readLine(false);
	}
}
