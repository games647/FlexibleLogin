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

    private transient boolean loggedIn;
    private transient boolean changed;

    //new account
    public Account(UUID uuid, String username, String password, byte[] ip) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = password;

        this.ip = ip;
        this.timestamp = null;

        this.loggedIn = true;
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
        this.changed = true;
        this.email = email;
    }

    public synchronized boolean isChanged() {
        return changed;
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
