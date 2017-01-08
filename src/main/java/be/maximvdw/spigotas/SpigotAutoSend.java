package be.maximvdw.spigotas;

import be.maximvdw.spigotas.config.Configuration;
import be.maximvdw.spigotas.storage.LogStorage;
import be.maximvdw.spigotas.ui.Console;
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.exceptions.SpamWarningException;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Spigot Auto Reply
 *
 * @author Maxim Van de Wynckel
 */
public class SpigotAutoSend {
    /* Authenticated user */
    private User user = null;
    private User conversationUser = null;
    private HashMap<Resource, List<User>> buyers = new HashMap<Resource, List<User>>();
    private boolean running = true;
    private LogStorage log = null;
    private String overrideUser = null;

    @SuppressWarnings("deprecation")
    public SpigotAutoSend(String... args) {
        Console.info("Initializing Spigot Auto Send ...");
        new SpigotSiteCore();
        new Configuration(2); // Version 2

        String username = Configuration.getString("username");
        String password = Configuration.getString("password");
        String totpSecret = Configuration.getString("2fakey");

        overrideUser = Configuration.getString("override-user");
        if (overrideUser.equals("")){
            overrideUser = null;
        }
        final int interval = Configuration.getInt("interval");

        Console.info("Logging in " + username + " ...");
        try {
            User user = SpigotSite.getAPI().getUserManager()
                    .authenticate(username, password, totpSecret);
            setUser(user);
        } catch (InvalidCredentialsException e) {
            Console.info("Unable to log in! Wrong credentials!");
            return;
        } catch (TwoFactorAuthenticationException e) {
            Console.info("Unable to log in! Two factor authentication failed!");
            return;
        }
        password = null;
        totpSecret = null;

        Console.info("Getting your premium plugins ...");
        if (user == null)
            return;
        List<Resource> resources = SpigotSite.getAPI().getResourceManager()
                .getResourcesByUser(user);
        for (Resource res : resources) {
            if (res instanceof PremiumResource) {
                Console.info("\t" + res.getResourceName());
                try {
                    List<User> resourceBuyers = SpigotSite
                            .getAPI()
                            .getResourceManager()
                            .getPremiumResourceBuyers(
                                    (PremiumResource) res, user);
                    buyers.put(
                            res,
                            resourceBuyers);
                    Console.info("\t\tBuyers: " + resourceBuyers.size());
                } catch (ConnectionFailedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
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
                                    recipients.add((overrideUser == null ? newBuyer.getUsername() : overrideUser));
                                    Console.info("Sending a message to "
                                            + (overrideUser == null ? newBuyer.getUsername() : overrideUser));

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

                                    if (!Configuration.getBoolean("debug")) {
                                        try {
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
                                        } catch (SpamWarningException ex) {
                                            Console.info("Spamming detected! ... Waiting for the next interval!");
                                            continue;
                                        }
                                    } else {
                                        Console.info("^ Above message was not send due to debug mode!");
                                    }

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

    public User getConversationUser() {
        return conversationUser;
    }

    public void setConversationUser(User conversationUser) {
        this.conversationUser = conversationUser;
    }
}
