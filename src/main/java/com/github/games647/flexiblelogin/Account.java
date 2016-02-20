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

import com.google.common.primitives.Longs;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

public class Account {

    private final UUID uuid;

    private String username;
    private String passwordHash;

    private byte[] ip;
    private Timestamp timestamp;

    private String email;

    private boolean loggedIn;

    //new account
    public Account(UUID uuid, String username, String password, byte[] ip) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = password;

        this.ip = ip;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    //existing account
    public Account(ResultSet resultSet) throws SQLException {
        //uuid in binary format
        byte[] uuidBytes = resultSet.getBytes(2);

        byte[] mostBits = ArrayUtils.subarray(uuidBytes, 0, 8);
        byte[] leastBits = ArrayUtils.subarray(uuidBytes, 8, 16);

        long mostByte = Longs.fromByteArray(mostBits);
        long leastByte = Longs.fromByteArray(leastBits);

        this.uuid = new UUID(mostByte, leastByte);
        this.username = resultSet.getString(3);
        this.passwordHash = resultSet.getString(4);

        this.ip = resultSet.getBytes(5);
        this.timestamp = resultSet.getTimestamp(6);

        this.email = resultSet.getString(7);
    }

    public boolean checkPassword(FlexibleLogin plugin, String userInput) throws Exception {
        return plugin.getHasher().checkPassword(passwordHash, userInput);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    /* package */ String getPassword() {
        return passwordHash;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public synchronized void setIp(byte[] ip) {
        this.ip = ip;
    }

    public synchronized void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized byte[] getIp() {
        return ip;
    }

    public synchronized String getIpString() {
        try {
            return InetAddress.getByAddress(ip).getHostName();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    public synchronized Timestamp getTimestamp() {
        return timestamp;
    }

    public synchronized String getEmail() {
        return email;
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
        this.loggedIn = loggedIn;
    }
}
