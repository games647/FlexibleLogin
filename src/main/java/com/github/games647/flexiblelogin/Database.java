package com.github.games647.flexiblelogin;

import com.github.games647.flexiblelogin.config.SQLConfiguration;
import com.github.games647.flexiblelogin.config.SQLType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.sql.SqlService;

@ThreadSafe
public class Database {

    private static final String USERS_TABLE = "users";

    private final FlexibleLogin plugin;
    //this cache is thread-safe
    private final Cache<UUID, Account> cache;

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private SqlService sql;

    public Database(FlexibleLogin plugin) {
        this.plugin = plugin;
        SQLConfiguration sqlConfig = plugin.getConfigManager().getConfiguration().getSqlConfiguration();

        if (sqlConfig.getType() == SQLType.MYSQL) {
            this.username = sqlConfig.getUsername();
            this.password = sqlConfig.getPassword();
        } else {
            //flat file drivers throw exception if you try to connect with a account
            this.username = "";
            this.password = "";
        }

        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(sqlConfig.getType().name().toLowerCase())
                .append("://");
        switch (sqlConfig.getType()) {
            case SQLITE:
                urlBuilder.append(sqlConfig.getPath()
                            .replace("%DIR%", plugin.getConfigManager().getConfigDir().getAbsolutePath()))
                        .append(File.separatorChar)
                        .append("database.db");
                break;
            case MYSQL:
                //jdbc:<engine>://[<username>[:<password>]@]<host>/<database> - copied from sponge doc
                urlBuilder.append(username).append(':').append(password).append("@")
                        .append(sqlConfig.getPath())
                        .append(':')
                        .append(sqlConfig.getPort())
                        .append('/')
                        .append(sqlConfig.getDatabase());
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

        return sql.getDataSource(jdbcUrl).getConnection();
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
                //check if the table already exists
                Statement statement = conn.createStatement();
                statement.execute("SELECT 1 FROM " + USERS_TABLE);
                statement.close();

                tableExists = true;
            } catch (SQLException sqlEx) {
                plugin.getLogger().debug("Table doesn't exist", sqlEx);
            }

            if (!tableExists) {
                if (sqlConfig.getType() == SQLType.SQLITE) {
                    Statement statement = conn.createStatement();
                    statement.execute("CREATE TABLE " + USERS_TABLE + " ( "
                            + "`UserID` INT UNSIGNED NOT NULL , "
                            + "`UUID` BINARY(16) NOT NULL , "
                            + "`Username` VARCHAR(32) NOT NULL , "
                            + "`Password` VARCHAR(64) NOT NULL , "
                            + "`IP` BINARY(32) NOT NULL , "
                            + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                            + "`Email` VARCHAR(64) DEFAULT NULL , "
                            + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                            + ")");
                    statement.close();
                } else {
                    Statement statement = conn.createStatement();
                    statement.execute("CREATE TABLE " + USERS_TABLE + " ( "
                            + "`UserID` INT UNSIGNED NOT NULL AUTO_INCREMENT , "
                            + "`UUID` BINARY(16) NOT NULL , "
                            + "`Username` VARCHAR(32) NOT NULL , "
                            + "`Password` VARCHAR(64) NOT NULL , "
                            + "`IP` BINARY(32) NOT NULL , "
                            + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                            + "`Email` VARCHAR(64) DEFAULT NULL , "
                            + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                            + ")");
                    statement.close();
            	}
                
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Error creating database table", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean deleteAccount(String playerName) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("DELETE FROM " + USERS_TABLE + " WHERE Username=?");
            statement.setString(1, playerName);

            int affectedRows = statement.executeUpdate();
            //remove cache entry if the player name is the key id
            cache.invalidate(playerName);
            //min one account was found
            return affectedRows > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("Error deleting user account", ex);
        } finally {
            closeQuietly(conn);
        }

        return false;
    }

    public boolean deleteAccount(UUID uuid) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("DELETE FROM " + USERS_TABLE + " WHERE UUID=?");

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            statement.setObject(1, Bytes.concat(mostBytes, leastBytes));

            int affectedRows = statement.executeUpdate();
            //removes the account from the cache
            cache.invalidate(uuid);

            //min one account was found
            return affectedRows > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("Error deleting user account", ex);
        } finally {
            closeQuietly(conn);
        }

        return false;
    }

    /**
     * @param player
     * @return null if the player doesn't exist
     */
    public Account loadAccount(Player player) {
        return loadAccount(player.getUniqueId());
    }

    public Account loadAccount(UUID uuid) {
        Account loadedAccount = cache.getIfPresent(uuid);
        if (loadedAccount == null) {
            Connection conn = null;
            try {
                conn = getConnection();
                PreparedStatement prepareStatement = conn.prepareStatement("SELECT * FROM " + USERS_TABLE
                        + " WHERE UUID=?");
                byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
                byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

                prepareStatement.setObject(1, Bytes.concat(mostBytes, leastBytes));

                ResultSet resultSet = prepareStatement.executeQuery();
                if (resultSet.next()) {
                    loadedAccount = new Account(resultSet);
                    cache.put(uuid, loadedAccount);
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
            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            prepareStatement.setObject(1, Bytes.concat(mostBytes, leastBytes));
            prepareStatement.setString(2, player.getName());
            prepareStatement.setString(3, password);

            byte[] ip = player.getConnection().getAddress().getAddress().getAddress();
            prepareStatement.setObject(4, ip);

            prepareStatement.execute();

            //if successfull
            cache.put(uuid, new Account(uuid, player.getName(), password, ip));
        } catch (SQLException ex) {
            plugin.getLogger().error("Error registering account", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    public void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                //this closes automatically the statement and resultset
                conn.close();
            } catch (SQLException ex) {
                //ingore
            }
        }
    }

    public void save(Account account) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("UPDATE " + USERS_TABLE
                    + " SET Username=?, Password=?, IP=?"
                    + " WHERE UUID=?");
            //username is now changeable by Mojang - so keep it up to date
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getPassword());
            statement.setObject(3, account.getIp());

            UUID uuid = account.getUuid();

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            statement.setObject(1, Bytes.concat(mostBytes, leastBytes));
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating user account", ex);
        } finally {
            closeQuietly(conn);
        }
    }
}
