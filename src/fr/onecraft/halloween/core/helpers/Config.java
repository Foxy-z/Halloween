package fr.onecraft.halloween.core.helpers;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Config {

    public static Configuration get(JavaPlugin plugin, String folder, String filename) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            File file = new File(
                    plugin.getDataFolder() + (folder.isEmpty() ? "" : "/") + folder,
                    filename + (filename.endsWith(".yml") ? "" : ".yml")
            );

            if (!file.exists()) return null;
            FileInputStream fileinputstream = new FileInputStream(file);
            configuration.load(new InputStreamReader(fileinputstream, Charset.forName("UTF-8")));
            return configuration;
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Configuration get(JavaPlugin plugin, String filename) {
        return get(plugin, "", filename);
    }

    public static Configuration get(JavaPlugin plugin, String folder, int filename) {
        return get(plugin, folder, String.valueOf(filename));
    }

    public static boolean save(JavaPlugin plugin, Configuration config, String folder, String filename) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            for (String key : config.getKeys(false)) configuration.set(key, config.get(key));

            configuration.save(new File(
                    plugin.getDataFolder() + (folder.isEmpty() ? "" : "/") + folder,
                    filename + (filename.endsWith(".yml") ? "" : ".yml")
            ));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean save(JavaPlugin plugin, Configuration config, String filename) {
        return save(plugin, config, "", filename);
    }

    public static boolean save(JavaPlugin plugin, Configuration config, String folder, int configName) {
        return save(plugin, config, folder, String.valueOf(configName));
    }
}
