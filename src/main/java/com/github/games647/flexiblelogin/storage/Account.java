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

import com.github.games647.flexiblelogin.hasher.Hasher;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;

public class Account {

    private final UUID uuid;

    private String username;
    private String passwordHash;
    private InetAddress ip;
    private String email;

    private Instant lastLogin;
    private transient boolean loggedIn;

    public Account(Player player, String password) {
        this(player.getUniqueId(), player.getName(), password, player.getConnection().getAddress().getAddress());
    }

    //new account
    public Account(UUID uuid, String username, String password, InetAddress ip) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = password;

        this.ip = ip;
        this.lastLogin = Instant.now();
    }

    public Account(UUID uuid, String username, String password, InetAddress ip, String email, Instant lastLogin) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = password;
        this.ip = ip;
        this.email = email;
        this.lastLogin = lastLogin;
    }

    public synchronized boolean checkPassword(Hasher hasher, String userInput) throws Exception {
        return hasher.checkPassword(passwordHash, userInput);
    }

    public static UUID getOfflineUUID(String playerName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    public UUID getId() {
        return uuid;
    }

    public synchronized Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    /* package */ synchronized String getPassword() {
        return passwordHash;
    }

    public synchronized void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public synchronized Optional<InetAddress> getIP() {
        return Optional.ofNullable(ip);
    }

    public synchronized void setIP(InetAddress ip) {
        this.ip = ip;
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

    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }

    public synchronized void setLoggedIn(boolean loggedIn) {
        if (loggedIn) {
            lastLogin = Instant.now();
        }

        this.loggedIn = loggedIn;
    }

    @Override
    public synchronized String toString() {
        return this.getClass().getSimpleName() + '{' +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", ip=" + ip +
                ", email='" + email + '\'' +
                ", loggedIn=" + loggedIn +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
