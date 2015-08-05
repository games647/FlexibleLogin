package com.github.games647.flexiblelogin;

import com.github.games647.flexiblelogin.config.SQLConfiguration;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.sql.SqlService;

public class Database {

    private static final String USERS_TABLE = "users";

    private final FlexibleLogin plugin;
    private final Cache<UUID, Account> cache;

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private SqlService sql;

    public Database(FlexibleLogin plugin) {
        this.plugin = plugin;
        SQLConfiguration sqlConfig = plugin.getConfigManager().getConfiguration().getSqlConfiguration();

        this.username = sqlConfig.getUsername();
        this.password = sqlConfig.getPassword();

        StringBuilder urlBuilder = new StringBuilder("jdbc")
                .append(':')
                .append(sqlConfig.getType().name().toLowerCase())
                .append(':');
        switch (sqlConfig.getType()) {
            case SQLITE:
                urlBuilder.append(sqlConfig.getPath()
                            .replace("%DIR%", plugin.getConfigManager().getConfigDir().getAbsolutePath()))
                        .append(File.separatorChar)
                        .append("database.db");
                break;
            case MYSQL:
                urlBuilder.append("//")
                        .append(sqlConfig.getPath())
                        .append(':')
                        .append(sqlConfig.getPort());
                break;
            case H2:
            default:
                urlBuilder.append(sqlConfig.getPath()
                            .replace("%DIR%", plugin.getConfigManager().getConfigDir().getAbsolutePath()))
                        .append(File.separatorChar)
                        .append("database");
                break;
        }

        this.jdbcUrl = urlBuilder.toString();

        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(1024)
                .build();
    }

    public Connection getConnection() throws SQLException {
        if (sql == null) {
            //lazy binding
            sql = plugin.getGame().getServiceManager().provide(SqlService.class).get();
        }

        return sql.getDataSource(jdbcUrl).getConnection(username, password);
    }

    public Account getAccountIfPresent(Player player) {
        return cache.getIfPresent(player.getUniqueId());
    }

    public boolean isLoggedin(Player player) {
        Account account = getAccountIfPresent(player);
        return account != null && account.isLoggedIn();
    }

    public void createTable() {
        Connection conn = null;
        try {
            conn = getConnection();

            boolean tableExists = false;
            try {
                Statement statement = conn.createStatement();
                statement.execute("SELECT 1 FROM " + USERS_TABLE);
                statement.close();

                tableExists = true;
            } catch (SQLException sqlEx) {
                plugin.getLogger().debug("Table doesn't exist", sqlEx);
            }

            if (!tableExists) {
                Statement statement = conn.createStatement();
                statement.execute("CREATE TABLE " + USERS_TABLE + " ( "
                        + "`UserID` INT UNSIGNED NOT NULL AUTO_INCREMENT , "
                        + "`UUID` BINARY(16) NOT NULL , "
                        + "`Username` VARCHAR(32) NOT NULL , "
                        + "`Password` VARCHAR(64) NOT NULL , "
                        + "`IP` BINARY(32) NOT NULL , "
                        + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                        + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                        + ")");
                statement.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Error creating database table", ex);
        } finally {
            //this closes automatically the statement and resultset
            closeQuietly(conn);
        }
    }

    /**
     * @param player
     * @return null if the player doesn't exist
     */
    public Account loadAccount(Player player) {
        Account loadedAccount = cache.getIfPresent(player.getUniqueId());
        if (loadedAccount == null) {
            Connection conn = null;
            try {
                conn = getConnection();
                PreparedStatement prepareStatement = conn.prepareStatement("SELECT * FROM " + USERS_TABLE
                        + " WHERE UUID=?");
                UUID uuid = player.getUniqueId();
                prepareStatement.setObject(1, uuid);

                ResultSet resultSet = prepareStatement.executeQuery();
                if (resultSet.next()) {
                    loadedAccount = new Account(resultSet);
                    cache.put(player.getUniqueId(), loadedAccount);
                }
            } catch (SQLException ex) {
                plugin.getLogger().error("Error loading account", ex);
            } finally {
                closeQuietly(conn);
            }
        }

        return loadedAccount;
    }

    public void createAccount(Player player, String password) {
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement prepareStatement = conn.prepareStatement("INSERT INTO " + USERS_TABLE
                    + " (UUID, Username, Password, IP) VALUES (?,?,?,?)");

            UUID uuid = player.getUniqueId();
            prepareStatement.setObject(1, uuid);
            prepareStatement.setString(2, player.getName());
            prepareStatement.setString(3, password);

            byte[] ip = player.getConnection().getAddress().getAddress().getAddress();
            prepareStatement.setObject(4, ip);

            prepareStatement.execute();

            //if successfull
            cache.put(uuid, new Account(uuid, player.getName(), password, ip));
        } catch (SQLException ex) {
            plugin.getLogger().error("Error registering account", ex);
        } catch (Exception ex) {
            plugin.getLogger().error("Unexpected error", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    public void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                //ingore
            }
        }
    }
}
