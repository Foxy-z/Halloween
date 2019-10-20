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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            CandyItem.loadTextures();
            Candy.loadLocations();
            Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).forEach(User::loadUser);
        });

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

    public void logToFile(String action, String message) {
        try {
            Date now = Calendar.getInstance().getTime();
            String today = new SimpleDateFormat("yyyy-MM-dd").format(now);
            String time = new SimpleDateFormat("HH:mm:ss").format(now);

            File file = new File(this.getDataFolder() + "/logs/", today + ".txt");
            if (!file.exists()) file.getParentFile().mkdirs();

            PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            writer.println("[" + time + "][" + action + "] " + message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void info(String message) {
        getLogger().info("[" + this.getName() + "] " + message);
    }

    private void error(String message) {
        getLogger().severe("[" + this.getName() + "] " + message);
    }
}
