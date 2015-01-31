package be.maximvdw.spigotar;

import java.util.ArrayList;
import java.util.List;

import be.maximvdw.spigotar.config.Configuration;
import be.maximvdw.spigotar.ui.Console;
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.user.Conversation;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;

/**
 * Spigot Auto Reply
 * 
 * @author Maxim Van de Wynckel
 */
public class SpigotAutoReply {
	/* Authenticated user */
	private User user = null;
	/* Conversation list */
	private List<Conversation> conversations = new ArrayList<Conversation>();
	private String message = "";
	private boolean running = true;

	public SpigotAutoReply(String... args) {
		Console.info("Initializing Spigot Auto Reply ...");
		new SpigotSiteCore();
		new Configuration(1); // Version 1

		String username = Configuration.getString("username");
		String password = Configuration.getString("password");
		final int interval = Configuration.getInt("interval");
		message = Configuration.getString("message");

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

		Console.info("Getting the latests private messages ...");
		if (user == null)
			return;
		List<Conversation> conversations = SpigotSite.getAPI()
				.getConversationManager().getConversations(getUser(), 20);
		for (Conversation conv : conversations) {
			Console.info("\t" + conv.getAuthor().getUsername() + ": "
					+ conv.getTitle());
		}
		setConversations(conversations);

		Thread th = new Thread(new Runnable() {

			public void run() {
				while (running) {
					try {
						Thread.sleep(interval * 1000);
						Console.info("Checking for new messages ...");
						List<Conversation> latestConversations = new ArrayList<Conversation>();
						latestConversations = SpigotSite.getAPI()
								.getConversationManager()
								.getConversations(user, 20);
						boolean hasSend = false;
						for (Conversation conv : latestConversations) {
							if (!getConversations().contains(conv)) {
								Console.info("Received a new message:");
								Console.info("\tTitle: " + conv.getTitle());
								Console.info("\tAuthor: "
										+ conv.getAuthor().getUsername());
								Console.info("\tReplies: "
										+ conv.getRepliesCount());
								if (hasSend) {
									Thread.sleep(15000);
									hasSend = false;
								}
								conv.reply(user, getMessage());
								hasSend = true;
							}
						}
						setConversations(latestConversations);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

		});
		th.start();
		Console.info("Press ENTER to quit.");
		System.console().readLine();
		running = false;
	}

	public static void main(String... args) {
		new SpigotAutoReply(args);
	}

	public User getUser() {
		return user;
	}

	private void setUser(User user) {
		this.user = user;
	}

	public List<Conversation> getConversations() {
		return conversations;
	}

	private void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
