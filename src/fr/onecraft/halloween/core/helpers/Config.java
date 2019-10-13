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

    public static void save(JavaPlugin plugin, Configuration config, String folder, String filename) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            for (String key : config.getKeys(false)) configuration.set(key, config.get(key));

            configuration.save(new File(
                    plugin.getDataFolder() + (folder.isEmpty() ? "" : "/") + folder,
                    filename + (filename.endsWith(".yml") ? "" : ".yml")
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
