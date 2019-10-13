package fr.onecraft.halloween;

import fr.onecraft.halloween.core.RegisterManager;
import fr.onecraft.halloween.core.database.DatabaseManager;
import fr.onecraft.halloween.core.database.exceptions.DatabaseConfigurationException;
import fr.onecraft.halloween.core.database.helpers.DatabaseConfig;
import fr.onecraft.halloween.core.objects.Candy;
import fr.onecraft.halloween.core.objects.CandyItem;
import fr.onecraft.halloween.core.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class Halloween extends JavaPlugin {
    public static Halloween INSTANCE;

    public static final String PREFIX = "§9Halloween > §7";
    public static final String ERROR = "§cErreur > §7";

    @Override
    public void onEnable() {
        info("Plugin is enabling...");
        INSTANCE = this;

        // copy default database configuration
        DatabaseConfig.copyDefaultConfig(this);

        // connect to database
        try {
            DatabaseManager.initDataBaseConnection(this);
        } catch (DatabaseConfigurationException e) {
            error("Unable to connect to the database, plugin will shut down...");
            e.printStackTrace();
            this.onDisable();
        }

        // load candy textures, candy locations and users
        CandyItem.loadTextures();
        Candy.loadLocations();
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).forEach(User::loadUser));

        // register events and commands
        new RegisterManager(this).register();

        info("Plugin has been enabled !");
    }

    @Override
    public void onDisable() {
        info("Plugin is disabling...");

        // disconnect database
        DatabaseManager.closeDataBaseConnection();

        info("Plugin has been disabled !");
    }

    private void info(String message) {
        getLogger().info("[" + this.getName() + "] " + message);
    }

    private void error(String message) {
        getLogger().severe("[" + this.getName() + "] " + message);
    }
}
