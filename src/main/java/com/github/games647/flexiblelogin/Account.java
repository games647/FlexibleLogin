/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;

public class Account {

    private final UUID uuid;

    private String username;
    private String passwordHash;

    private byte[] ip;
    private String email;

    private boolean loggedIn;
    private Instant lastLogin;

    public Account(Player player, String password) {
        this(player.getUniqueId()
                , player.getName(), password, player.getConnection().getAddress().getAddress().getAddress());
    }

    //new account
    public Account(UUID uuid, String username, String password, byte[] ip) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = password;

        this.ip = Arrays.copyOf(ip, ip.length);
        this.lastLogin = Instant.now();
    }

    //existing account
    public Account(ResultSet resultSet) throws SQLException {
        //uuid in binary format
        ByteBuffer uuidBytes = ByteBuffer.wrap(resultSet.getBytes(2));

        this.uuid = new UUID(uuidBytes.getLong(), uuidBytes.getLong());
        this.username = resultSet.getString(3);
        this.passwordHash = resultSet.getString(4);

        this.ip = resultSet.getBytes(5);
        this.lastLogin = resultSet.getTimestamp(6).toInstant();

        this.email = resultSet.getString(7);
    }

    public synchronized boolean checkPassword(FlexibleLogin plugin, String userInput) throws Exception {
        return plugin.getHasher().checkPassword(passwordHash, userInput);
    }

    public UUID getUuid() {
        return uuid;
    }

    public synchronized String getUsername() {
        return username;
    }

    /* package */ synchronized String getPassword() {
        return passwordHash;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public synchronized void setIp(byte[] ip) {
        this.ip = Arrays.copyOf(ip, ip.length);
    }

    public synchronized byte[] getIp() {
        return Arrays.copyOf(ip, ip.length);
    }

    public synchronized Optional<String> getIpString() {
        try {
            return Optional.of(InetAddress.getByAddress(ip).getHostName());
        } catch (UnknownHostException ex) {
            return Optional.empty();
        }
    }

    public synchronized Instant getLastLogin() {
        return lastLogin;
    }

    public synchronized Optional<String> getEmail() {
        if (email == null || email.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(email);
    }

    public synchronized void setEmail(String email) {
        this.email = email;
    }

    //these methods have to thread-safe as they will be accessed
    //through Async (PlayerChatEvent/LoginTask) and sync methods
    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }

    public synchronized void setLoggedIn(boolean loggedIn) {
        if (loggedIn) {
            lastLogin = Instant.now();
        }

        this.loggedIn = loggedIn;
    }
}
