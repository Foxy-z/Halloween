package fr.onecraft.halloween.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseAccess {
    private final DatabaseCredentials credentials;
    private HikariDataSource hikariDataSource;

    DatabaseAccess(DatabaseCredentials credentials) {
        this.credentials = credentials;
    }

    private void setUpHikariCP(Plugin plugin) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(this.credentials.toURI());
        hikariConfig.setUsername(this.credentials.getUser());
        hikariConfig.setPassword(this.credentials.getPass());

        hikariConfig.setMaximumPoolSize(4);
        hikariConfig.setMaxLifetime(30000L);
        hikariConfig.setIdleTimeout(30000L);
        hikariConfig.setLeakDetectionThreshold(10000L);
        hikariConfig.setConnectionTimeout(10000L);
        hikariConfig.setPoolName(plugin.getServer().getServerName() + ":" + plugin.getName());
        hikariConfig.setMinimumIdle(2);

        hikariConfig.addDataSourceProperty("autoReconnect", true);
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void initPool(Plugin plugin) {
        setUpHikariCP(plugin);
    }

    public void closePool() {
        this.hikariDataSource.close();
    }

    public Connection getConnection() throws SQLException {
        return this.hikariDataSource.getConnection();
    }
}