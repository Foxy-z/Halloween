package fr.onecraft.halloween.core.database;

import fr.onecraft.halloween.core.database.exceptions.DatabaseConfigurationException;
import fr.onecraft.halloween.core.database.helpers.DatabaseConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseAccess dataBase;

    public static Connection getConnection() throws SQLException {
        return dataBase.getConnection();
    }

    public static void initDataBaseConnection(JavaPlugin plugin) throws DatabaseConfigurationException {
        dataBase = new DatabaseAccess(DatabaseConfig.getCredentials(plugin));
        dataBase.initPool(plugin);
    }

    public static void closeDataBaseConnection() {
        if (dataBase != null) dataBase.closePool();
    }
}