package be.maximvdw.spigotas;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import be.maximvdw.spigotas.config.Configuration;
import be.maximvdw.spigotas.storage.LogStorage;
import be.maximvdw.spigotas.ui.Console;
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;

/**
 * Spigot Auto Reply
 * 
 * @author Maxim Van de Wynckel
 */
public class SpigotAutoSend {
	/* Authenticated user */
	private User user = null;
	private HashMap<Resource, List<User>> buyers = new HashMap<Resource, List<User>>();
	private boolean running = true;
	private LogStorage log = null;

	@SuppressWarnings("deprecation")
	public SpigotAutoSend(String... args) {
		Console.info("Initializing Spigot Auto Send ...");
		new SpigotSiteCore();
		new Configuration(1); // Version 1

		String username = Configuration.getString("username");
		String password = Configuration.getString("password");
		final int interval = Configuration.getInt("interval");

		Console.info("Logging in " + username + " ...");
		try {
			User user = SpigotSite.getAPI().getUserManager()
					.authenticate(username, password);
			setUser(user);
		} catch (InvalidCredentialsException e) {
			Console.info("Unable to log in! Wrong credentials!");
			return;
		}
		password = null;

		Console.info("Getting your premium plugins ...");
		if (user == null)
			return;
		List<Resource> resources = SpigotSite.getAPI().getResourceManager()
				.getResourcesByUser(user);
		for (Resource res : resources) {
			if (res instanceof PremiumResource) {
				Console.info("\t" + res.getResourceName());
				buyers.put(res, SpigotSite.getAPI().getResourceManager()
						.getPremiumResourceBuyers((PremiumResource) res, user));
			}
		}

		if (buyers.isEmpty()) {
			Console.info("You do not have any premium plugins!");
			return;
		}

		log = new LogStorage("log");
		logMessage("Starting now ...");

		Thread th = new Thread(new Runnable() {

			public void run() {
				while (running) {
					try {
						Thread.sleep(interval * 1000);
						boolean hasSend = false;
						for (Resource res : buyers.keySet()) {
							Console.info("Checking for new buyers in: "
									+ res.getResourceName());
							List<User> newBuyers = SpigotSite
									.getAPI()
									.getResourceManager()
									.getPremiumResourceBuyers(
											(PremiumResource) res, user);
							if (newBuyers.isEmpty() && (!buyers.isEmpty()))
								continue;
							for (User newBuyer : newBuyers) {
								if (!buyers.get(res).contains(newBuyer)) {
									Set<String> recipients = new HashSet<String>();
									recipients.add(newBuyer.getUsername());
									Console.info("Sending a message to "
											+ newBuyer.getUsername());

									if (hasSend) {
										Console.info("Waiting before sending to prevent spam ...");
										Thread.sleep(15000);
										hasSend = false;
									}
									String title = Configuration
											.getString("title")
											.replace("{plugin}",
													res.getResourceName())
											.replace("{member}",
													newBuyer.getUsername());
									String message = Configuration
											.getString("message")
											.replace("{plugin}",
													res.getResourceName())
											.replace("{member}",
													newBuyer.getUsername());

									logMessage("[BUYER] "
											+ newBuyer.getUsername() + " ["
											+ newBuyer.getUserId()
											+ "] bought "
											+ res.getResourceName());

									if (!Configuration.getBoolean("debug"))
										SpigotSite
												.getAPI()
												.getConversationManager()
												.createConversation(
														user,
														recipients,
														title,
														message,
														Configuration
																.getBoolean("options.lock"),
														false, false);

									hasSend = true;
								}

							}

							buyers.put(res, newBuyers);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

		});
		th.start();
		Console.info("Press ENTER to quit.");
		System.console().readLine();
		th.stop();
		running = false;
	}

	private void logMessage(String message) {
		Date today;
		String output;
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
		today = new Date();
		output = formatter.format(today);
		String resultMessage = "[" + output + "] " + message;
		log.appendLog(resultMessage);
	}

	public static void main(String... args) {
		new SpigotAutoSend(args);
	}

	public User getUser() {
		return user;
	}

	private void setUser(User user) {
		this.user = user;
	}
}
