/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
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
package com.github.games647.flexiblelogin.storage;

import com.github.games647.flexiblelogin.config.SQLConfig.Type;
import com.github.games647.flexiblelogin.config.Settings;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

public class FlexibleDatabase extends Database {

    public FlexibleDatabase(Logger logger, Settings settings) throws SQLException {
        super(logger, settings, "flexiblelogin_users");
    }

    @Override
    public void createTable(Type type) throws SQLException, IOException {
        Asset asset = Sponge.getAssetManager().getAsset("create.sql").get();
        StringBuilder builder = new StringBuilder();

        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement()) {
            for (String line : asset.readLines()) {
                builder.append(line.replace("{TABLE_NAME}", tableName));
                if (line.endsWith(";")) {
                    String sql = builder.toString();
                    if (type == Type.SQLITE) {
                        sql = sql.replace("AUTO_INCREMENT", "AUTOINCREMENT");
                    }

                    stmt.addBatch(sql);
                    builder = new StringBuilder();
                }
            }

            stmt.executeBatch();
        }
    }

    @Override
    public Optional<String> exists(String username) {
        return exists(username, "Username");
    }

    @Override
    public Optional<Account> loadAccount(UUID uuid) {
        String sql = "SELECT * FROM " + tableName + " WHERE UUID=?";
        return loadAccount(sql, stmt -> stmt.setObject(1, toArray(uuid)));
    }

    @Override
    public Optional<Account> loadAccount(String playerName) {
        String sql = "SELECT * FROM " + tableName + " WHERE Username=?";
        return loadAccount(sql, stmt -> stmt.setString(1, playerName));
    }

    @Override
    public boolean deleteAccount(UUID uuid) {
        if (deleteAccount("UUID", stmt -> stmt.setObject(1, toArray(uuid)))) {
            cache.remove(uuid);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteAccount(String playerName) {
        return deleteAccount("Username", stmt -> stmt.setString(1, playerName));
    }

    @Override
    public int getRegistrationsCount(InetAddress ip) {
        return getRegistrationsCount(ip, stmt -> stmt.setBytes(1, ip.getAddress()));
    }

    @Override
    public boolean createAccount(Account account) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + tableName
                     + " (Username, Password, IP, LastLogin, Email, LoggedIn, UUID) VALUES (?,?,?,?,?,?,?)")) {
            writeAccount(stmt, account);
            stmt.executeUpdate();
            return true;
        } catch (SQLException sqlEx) {
            logger.error("Error registering account", sqlEx);
            return false;
        }
    }

    @Override
    public boolean save(Account account) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("UPDATE " + tableName
                + " SET Username=?, Password=?, IP=?, LastLogin=?, Email=?, LoggedIn=? WHERE UUID=?")) {
            writeAccount(stmt, account);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.error("Error saving user account", ex);
            return false;
        }
    }

    private byte[] toArray(UUID uuid) {
        return ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    private void writeAccount(PreparedStatement stmt, Account account) throws SQLException {
        stmt.setString(1, account.getUsername());
        stmt.setString(2, account.getPassword());
        stmt.setObject(3, account.getIP().getAddress());

        stmt.setTimestamp(4, Timestamp.from(account.getLastLogin()));
        stmt.setString(5, account.getEmail().orElse(null));
        stmt.setBoolean(6, account.isLoggedIn());

        stmt.setObject(7, toArray(account.getId()));
    }

    @Override
    protected Optional<Account> parseLoadResult(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            return Optional.empty();
        }

        UUID uuid = parseUUID(resultSet.getBytes(2));
        String username = resultSet.getString(3);
        String password = resultSet.getString(4);

        InetAddress ip = parseAddress(resultSet.getBytes(5));
        Instant lastLogin = parseTimestamp(resultSet, 6);
        String email = resultSet.getString(7);

        return Optional.of(new Account(uuid, username, password, ip, email, lastLogin));
    }

    private InetAddress parseAddress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private UUID parseUUID(byte[] bytes) {
        ByteBuffer uuidBytes = ByteBuffer.wrap(bytes);
        return new UUID(uuidBytes.getLong(), uuidBytes.getLong());
    }

    @Override
    public void close() {
        //set all player accounts existing in the database to not logged in
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE " + tableName + " SET LoggedIn=0");
        } catch (SQLException ex) {
            logger.error("Error updating login status", ex);
        }

        super.close();
    }
}
