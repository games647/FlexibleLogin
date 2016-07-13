/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;

public class Database {

    public static final String USERS_TABLE = "flexiblelogin_users";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();
    //this cache is thread-safe
    private final Cache<UUID, Account> cache;

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private SqlService sql;

    public Database() {
        SQLConfiguration sqlConfig = plugin.getConfigManager().getConfig().getSqlConfiguration();

        if (sqlConfig.getType() == SQLType.MYSQL) {
            this.username = sqlConfig.getUsername();
            this.password = sqlConfig.getPassword();
        } else {
            //flat file drivers throw exception if you try to connect with a account
            this.username = "";
            this.password = "";
        }

        String storagePath = sqlConfig.getPath()
                .replace("%DIR%", plugin.getConfigManager().getConfigDir().getAbsolutePath());

        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(sqlConfig.getType().name().toLowerCase()).append("://");
        switch (sqlConfig.getType()) {
            case SQLITE:
                urlBuilder.append(storagePath).append(File.separatorChar).append("database.db");
                break;
            case MYSQL:
                //jdbc:<engine>://[<username>[:<password>]@]<host>/<database> - copied from sponge doc
                urlBuilder.append(username).append(':').append(password).append('@')
                        .append(sqlConfig.getPath())
                        .append(':')
                        .append(sqlConfig.getPort())
                        .append('/')
                        .append(sqlConfig.getDatabase());
                break;
            case H2:
            default:
                urlBuilder.append(storagePath).append(File.separatorChar).append("database");
                break;
        }

        this.jdbcUrl = urlBuilder.toString();
        cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maximumSize(1024).build();
    }

    public Connection getConnection() throws SQLException {
        if (sql == null) {
            //lazy binding
            sql = plugin.getGame().getServiceManager().provideUnchecked(SqlService.class);
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
        try {
            DatabaseMigration migration = new DatabaseMigration(plugin);
            migration.migrateName();
            migration.createTable();
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error creating database table", sqlEx);
        }
    }

    public boolean deleteAccount(String playerName) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("DELETE FROM " + USERS_TABLE + " WHERE Username=?");
            statement.setString(1, playerName);

            int affectedRows = statement.executeUpdate();
            //remove cache entry
            Set<Map.Entry<UUID, Account>> cacheEntries = cache.asMap().entrySet();
            for (Map.Entry<UUID, Account> cacheEntry : cacheEntries) {
                Account account = cacheEntry.getValue();
                if (account.getUsername().equals(playerName)) {
                    cache.invalidate(account.getUuid());
                    break;
                }
            }

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
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error deleting user account", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return false;
    }

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
            } catch (SQLException sqlEx) {
                plugin.getLogger().error("Error loading account", sqlEx);
            } finally {
                closeQuietly(conn);
            }
        }

        return loadedAccount;
    }

    public Account loadAccount(String playerName) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + USERS_TABLE + " WHERE Username=?");
            statement.setString(1, playerName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Account loadedAccount = new Account(resultSet);
                return loadedAccount;
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading account", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return null;
    }

    public int getRegistrationsCount(byte[] ip) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE IP=?");
            statement.setBytes(1, ip);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading count of registrations", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return -1;
    }

    public Account createAccount(Player player, String password) {
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement prepareStatement = conn.prepareStatement("INSERT INTO " + USERS_TABLE
                    + " (UUID, Username, Password, IP, Email, LastLogin) VALUES (?,?,?,?,?,?)");

            UUID uuid = player.getUniqueId();
            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            byte[] ip = player.getConnection().getAddress().getAddress().getAddress();
            Account account = new Account(uuid, player.getName(), password, ip);

            prepareStatement.setObject(1, Bytes.concat(mostBytes, leastBytes));
            prepareStatement.setString(2, player.getName());
            prepareStatement.setString(3, password);

            prepareStatement.setObject(4, ip);

            prepareStatement.setString(5, account.getEmail());
            prepareStatement.setTimestamp(6, account.getTimestamp());

            prepareStatement.execute();

            //if successfull
            cache.put(uuid, account);
            return account;
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error registering account", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return null;
    }

    protected void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                //this closes automatically the statement and resultset
                conn.close();
            } catch (SQLException ex) {
                //ingore
            }
        }
    }

    public void flushLoginStatus(Account account, boolean loggedIn) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement prepareStatement = conn.prepareStatement("UPDATE " + USERS_TABLE
                    + " SET LoggedIn=? WHERE UUID=?");

            prepareStatement.setInt(1, loggedIn ? 1 : 0);

            UUID uuid = account.getUuid();
            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            prepareStatement.setObject(2, Bytes.concat(mostBytes, leastBytes));

            prepareStatement.execute();
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating login status", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    public void close() {
        //clear cache
        cache.invalidateAll();

        Connection conn = null;
        try {
            conn = getConnection();

            //set all player accounts existing in the database to unlogged
            conn.createStatement().execute("UPDATE " + USERS_TABLE + " SET LoggedIn=0");
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating user account", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean save(Account account) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("UPDATE " + USERS_TABLE
                    + " SET Username=?, Password=?, IP=?, LastLogin=?, Email=? WHERE UUID=?");
            //username is now changeable by Mojang - so keep it up to date
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getPassword());
            statement.setObject(3, account.getIp());

            statement.setTimestamp(4, account.getTimestamp());
            statement.setString(5, account.getEmail());

            UUID uuid = account.getUuid();

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            statement.setObject(6, Bytes.concat(mostBytes, leastBytes));
            statement.execute();
            return true;
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating user account", ex);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }
}
