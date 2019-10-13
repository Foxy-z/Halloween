package fr.onecraft.halloween.core.database.helpers;

import fr.onecraft.halloween.core.database.DatabaseCredentials;
import fr.onecraft.halloween.core.database.exceptions.DatabaseConfigurationException;
import fr.onecraft.halloween.core.helpers.Config;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DatabaseConfig {
    private static final String CONFIG_PATH = "config";
    private static final String CONFIG_NAME = "database.yml";

    public static DatabaseCredentials getCredentials(JavaPlugin plugin) throws DatabaseConfigurationException {
        Configuration configuration = Config.get(plugin, CONFIG_PATH, CONFIG_NAME);
        if (configuration == null) {
            throw new DatabaseConfigurationException("Database configuration file is not valid");
        }

        if (!configuration.contains("host") ||
                !configuration.contains("password") ||
                !configuration.contains("port") ||
                !configuration.contains("user") ||
                !configuration.contains("name")) {
            throw new DatabaseConfigurationException("Parameter forgotten in the database configuration file");
        }

        return new DatabaseCredentials(
                configuration.getString("host"),
                configuration.getString("password"),
                configuration.getInt("port"),
                configuration.getString("user"),
                configuration.getString("name")
        );
    }

    public static void copyDefaultConfig(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder() + "/" + CONFIG_PATH, CONFIG_NAME);
        if (!configFile.exists()) {
            plugin.getLogger().info("Default database configuration file moved to " + CONFIG_PATH + "/" + CONFIG_NAME);

            InputStream stream = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_NAME);
            YamlConfiguration configuration = new YamlConfiguration();

            if (stream != null) {
                try {
                    configuration.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    stream.close();
                } catch (IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().severe("Default configuration file invalid");
            }

            Config.save(plugin, configuration, CONFIG_PATH, CONFIG_NAME);
        }
    }
}
