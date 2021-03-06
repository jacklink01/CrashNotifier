package com.jacklinkproductions.CrashNotifier;

import java.io.File;
import org.apache.logging.log4j.LogManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    static PluginDescriptionFile pdfFile;

	public static Integer configVersion = 3;
	public static int updaterID = 54918;
    
    @Override
    public void onDisable() {
        // Output info to console on disable
        getLogger().info("Thanks for using CrashNotifier!");
    }

    @Override
    public void onEnable() {
    	
        // Create default config if not exist yet.
        if (!new File(getDataFolder(), "config.yml").exists()) {
            getLogger().info( "Creating config.yml" );
            saveDefaultConfig();
        }

        // Load configuration
        reloadConfiguration();
        
        // Check for old config
        if ((getConfig().isSet("config-version") == false) || (getConfig().getInt("config-version") < configVersion))
        {
            File file = new File(this.getDataFolder(), "config.yml");
            file.delete();
            saveDefaultConfig();
            getLogger().info( "Created a new config.yml for this version." );
        }

        // Setup Updater system
        if (getConfig().getString("update-notification") == "true")
        {
        	checkForUpdates();
        }
        
        // Register our events
        getServer().getPluginManager().registerEvents(new CrashListener(this), this);
        //getLogger().addFilter(new CrashNotifierFilter(this));

        // Register logger (1.7 HAX) :(
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new CrashNotifierFilter(this));

        // Register command executor.
        CrashCommand crashCommandExecutor = new CrashCommand(this);
        getCommand("crash").setExecutor(crashCommandExecutor);
        getCommand("crashnotifier").setExecutor(crashCommandExecutor);

        // Output info to console on load
        pdfFile = this.getDescription();
        getLogger().info( pdfFile.getName() + " v" + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void reloadConfiguration() {
        reloadConfig();
        CrashListener.joinmessage = getConfig().getString("default-messages.join");
        CrashListener.quitmessage = getConfig().getString("default-messages.quit");
        CrashListener.kickmessage = getConfig().getString("crash-messages.kick");
        CrashListener.spammessage = getConfig().getString("crash-messages.spam");
        CrashListener.hostmessage = getConfig().getString("crash-messages.remoteHost");
        CrashListener.softwaremessage = getConfig().getString("crash-messages.softwareHost");
        CrashListener.timeoutmessage = getConfig().getString("crash-messages.readTimeout");
        CrashNotifierFilter.loginattempts = getConfig().getString("showLoginAttempts");
    }

	public static String parseColor(String line)
	{
		return ChatColor.translateAlternateColorCodes('&', line);
	}

    public boolean updateAvailable = false;
    String latestVersion = null;

    public void checkForUpdates()
    {
        final Updater updater = new Updater(this, updaterID, getFile(), Updater.UpdateType.NO_DOWNLOAD, true); // Start Updater but just do a version check
        updateAvailable = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; // Determine if there is an update ready for us
        latestVersion = updater.getLatestName();
        getLogger().info(latestVersion + " is the latest version available, and the updatability of it is: " + updater.getResult().name());

        if(updateAvailable)
        {
            for (Player player : getServer().getOnlinePlayers())
            {
                if (player.hasPermission("lampcontrol.update"))
                {
                    player.sendMessage(ChatColor.YELLOW + "An update is available: " + latestVersion);
                    player.sendMessage(ChatColor.YELLOW + "Type /crash update if you would like to update.");
                }
            }

            getServer().getPluginManager().registerEvents(new Listener()
            {
                @EventHandler
                public void onPlayerJoin (PlayerJoinEvent event)
                {
                    Player player = event.getPlayer();
                    if (player.hasPermission("lampcontrol.update"))
                    {
                        player.sendMessage(ChatColor.YELLOW + "An update is available: " + latestVersion);
                        player.sendMessage(ChatColor.YELLOW + "Type /crash update if you would like to update.");
                    }
                }
            }, this);
        }
    }
    
    @Override
    public File getFile() {

        return super.getFile();
    }
}