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
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;

public class Database {

    public static final String USERS_TABLE = "flexiblelogin_users";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();
    private final Map<UUID, Account> cache = Maps.newConcurrentMap();
    private final DataSource dataSource;

    public Database() throws SQLException {
        SQLConfiguration sqlConfig = plugin.getConfigManager().getGeneral().getSQL();

        Path configDir = plugin.getConfigManager().getConfigDir();
        String storagePath = sqlConfig.getPath().replace("%DIR%", configDir.normalize().toString());

        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(sqlConfig.getType().name().toLowerCase()).append("://");
        switch (sqlConfig.getType()) {
            case SQLITE:
                urlBuilder.append(storagePath).append(File.separatorChar).append("database.db");
                break;
            case MYSQL:
                //jdbc:<engine>://[<username>[:<password>]@]<host>/<database> - copied from sponge doc
                urlBuilder.append(sqlConfig.getUsername())
                        .append(':')
                        .append(sqlConfig.getPassword())
                        .append('@')
                        .append(sqlConfig.getPath())
                        .append(':')
                        .append(sqlConfig.getPort())
                        .append('/')
                        .append(sqlConfig.getDatabase())
                        .append("?useSSL")
                        .append('=')
                        .append(sqlConfig.isUseSSL());
                break;
            case H2:
            default:
                urlBuilder.append(storagePath).append(File.separatorChar).append("database");
                break;
        }

        String jdbcUrl = urlBuilder.toString();
        this.dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(jdbcUrl);
    }

    public Account getAccountIfPresent(Player player) {
        return cache.get(player.getUniqueId());
    }

    public boolean isLoggedin(Player player) {
        Account account = getAccountIfPresent(player);
        return account != null && account.isLoggedIn();
    }

    public void createTable() {
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " ( "
                    + "`UserID` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + "`UUID` BINARY(16) NOT NULL, "
                    + "`Username` VARCHAR, "
                    + "`Password` VARCHAR(64) NOT NULL, "
                    + "`IP` BINARY(32) NOT NULL, "
                    + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "`Email` VARCHAR(64) DEFAULT NULL, "
                    + "`LoggedIn` BOOLEAN DEFAULT 0, "
                    + "UNIQUE (`UUID`) "
                    + ')';
            if (plugin.getConfigManager().getGeneral().getSQL().getType() == SQLType.SQLITE) {
                createTable = createTable.replace("AUTO_INCREMENT", "");
            }

            statement.execute(createTable);
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error creating database table", sqlEx);
        }
    }

    public boolean deleteAccount(String playerName) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + USERS_TABLE + " WHERE Username=?")) {
            stmt.setString(1, playerName);

            int affectedRows = stmt.executeUpdate();
            //remove cache entry
            cache.values().stream()
                    .filter(account -> account.getUsername().equals(playerName))
                    .map(Account::getUuid)
                    .forEach(cache::remove);

            //min one account was found
            return affectedRows > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("Error deleting user account", ex);
        }

        return false;
    }

    public boolean deleteAccount(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + USERS_TABLE + " WHERE UUID=?")) {
            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            stmt.setObject(1, Bytes.concat(mostBytes, leastBytes));

            int affectedRows = stmt.executeUpdate();
            //removes the account from the cache
            cache.remove(uuid);

            //min one account was found
            return affectedRows > 0;
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error deleting user account", sqlEx);
        }

        return false;
    }

    public boolean exists(String username) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT USERNAME FROM " + USERS_TABLE
                     + " WHERE LOWER(USERNAME) = ?")) {
            stmt.setString(1, username.toLowerCase());

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error deleting user account", sqlEx);
        }

        return true;
    }

    public Optional<Account> loadAccount(Player player) {
        return loadAccount(player.getUniqueId());
    }

    public Account remove(Player player) {
        return cache.remove(player.getUniqueId());
    }

    public Optional<Account> loadAccount(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + USERS_TABLE
                     + " WHERE UUID=?")) {

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            stmt.setObject(1, Bytes.concat(mostBytes, leastBytes));

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    Account loadedAccount = new Account(resultSet);
                    cache.put(uuid, loadedAccount);
                    return Optional.of(loadedAccount);
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading account", sqlEx);
        }

        return Optional.empty();
    }

    public Optional<Account> loadAccount(String playerName) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + USERS_TABLE + " WHERE Username=?")) {
            stmt.setString(1, playerName);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Account(resultSet));
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading account", sqlEx);
        }

        return Optional.empty();
    }

    public int getRegistrationsCount(byte[] ip) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + USERS_TABLE
                     + " WHERE IP=?")) {
            stmt.setBytes(1, ip);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading count of registrations", sqlEx);
        }

        return -1;
    }

    public boolean createAccount(Account account, boolean shouldCache) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + USERS_TABLE
                     + " (UUID, Username, Password, IP, Email, LastLogin) VALUES (?,?,?,?,?,?)")) {
            UUID uuid = account.getUuid();
            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            stmt.setObject(1, Bytes.concat(mostBytes, leastBytes));
            stmt.setString(2, account.getUsername());
            stmt.setString(3, account.getPassword());

            stmt.setObject(4, account.getIp());

            stmt.setString(5, account.getEmail());
            stmt.setTimestamp(6, Timestamp.from(account.getLastLogin()));

            stmt.execute();

            if (shouldCache) {
                cache.put(uuid, account);
            }

            return true;
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error registering account", sqlEx);
        }

        return false;
    }

    public void flushLoginStatus(Account account, boolean loggedIn) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE " + USERS_TABLE
                     + " SET LoggedIn=? WHERE UUID=?")) {
            stmt.setInt(1, loggedIn ? 1 : 0);

            UUID uuid = account.getUuid();
            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            stmt.setObject(2, Bytes.concat(mostBytes, leastBytes));
            stmt.execute();
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating login status", ex);
        }
    }

    public void close() {
        cache.clear();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            //set all player accounts existing in the database to unlogged
            stmt.execute("UPDATE " + USERS_TABLE + " SET LoggedIn=0");
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating loggin status", ex);
        }
    }

    public boolean save(Account account) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("UPDATE " + USERS_TABLE
                     + " SET Username=?, Password=?, IP=?, LastLogin=?, Email=? WHERE UUID=?")) {
            //username is now changeable by Mojang - so keep it up to date
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.setObject(3, account.getIp());

            stmt.setTimestamp(4, Timestamp.from(account.getLastLogin()));
            stmt.setString(5, account.getEmail());

            UUID uuid = account.getUuid();

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            stmt.setObject(6, Bytes.concat(mostBytes, leastBytes));
            stmt.execute();
            cache.put(uuid, account);
            return true;
        } catch (SQLException ex) {
            plugin.getLogger().error("Error updating user account", ex);
            return false;
        }
    }
}
