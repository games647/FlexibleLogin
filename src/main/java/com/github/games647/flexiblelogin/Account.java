package com.github.games647.flexiblelogin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

public class Account {

    private final UUID uuid;
    private final String username;
    private final String passwordHash;

    private final byte[] ip;
    private final Timestamp timestamp;

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

        byte[] mostBits = ArrayUtils.subarray(uuidBytes, 0, 3);
        byte[] leastBits = ArrayUtils.subarray(uuidBytes, 3, 7);

        this.uuid = new UUID(parseMostSignificant(mostBits), parseLeastSignificant(leastBits));
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

    public byte[] getIp() {
        return ip;
    }

    public Timestamp getTimestamp() {
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

    private long parseLeastSignificant(byte[] byteArray) {
        long value = 0;
        for (int i = 0; i < byteArray.length; i++) {
            value += ((long) byteArray[i] & 0xffL) << (8 * i);
        }

        return value;
    }

    private long parseMostSignificant(byte[] byteArray) {
        long value = 0;
        for (int i = 0; i < byteArray.length; i++) {
            value = (value << 8) + (byteArray[i] & 0xff);
        }

        return value;
    }
}
