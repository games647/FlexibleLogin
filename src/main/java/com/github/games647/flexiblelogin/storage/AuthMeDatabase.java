package com.github.games647.flexiblelogin.storage;

import com.github.games647.flexiblelogin.config.Settings;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.User;

public class AuthMeDatabase extends Database {

    public AuthMeDatabase(Logger logger, Settings settings) throws SQLException {
        super(logger, settings, settings.getGeneral().getSQL().getAuthMeTable());
    }

    @Override
    public Optional<Account> loadAccount(UUID uuid) {
        //AuthMe doesn't use UUIDs
        return Optional.empty();
    }

    @Override
    public boolean deleteAccount(UUID uuid) {
        //AuthMe doesn't use UUIDs
        return false;
    }

    @Override
    public Optional<Account> loadAccount(User player) {
        Optional<Account> optAcc = loadAccount(player.getName());
        optAcc.ifPresent(account -> cache.put(player.getUniqueId(), account));
        return optAcc;
    }

    @Override
    public Optional<String> exists(String username) {
        return exists(username, "realname");
    }

    @Override
    public Optional<Account> loadAccount(String playerName) {
        String sql = "SELECT realname, password, ip, lastlogin, email FROM " + tableName + " WHERE realname=?";
        return loadAccount(sql, stmt -> stmt.setString(1, playerName));
    }

    @Override
    public boolean deleteAccount(String playerName) {
        return deleteAccount("realname", stmt -> stmt.setString(1, playerName));
    }

    @Override
    public int getRegistrationsCount(InetAddress ip) {
        return getRegistrationsCount(ip, stmt -> stmt.setString(1, ip.getHostAddress()));
    }
    
    @Override
    public Set<Account> getAccountsByIp(InetAddress ip) {
        return getAccountsByIp(ip, stmt -> stmt.setBytes(1, ip.getAddress()));
    }
    
    @Override
    public boolean createAccount(Account account) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + tableName
                     + " (username, realname, password, ip, lastlogin, email) VALUES (?,?,?,?,?,?)")) {
            writeAccount(stmt, account);
            stmt.executeUpdate();

            return true;
        } catch (SQLException sqlEx) {
            logger.error("Error registering account", sqlEx);
        }

        return false;
    }

    @Override
    public boolean save(Account account) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("UPDATE " + tableName + " SET realname=?, realname=?, "
                     + "password=?, ip=?, lastlogin=?, email=?, isLogged=? WHERE realname=?")) {
            writeAccount(stmt, account);

            stmt.setBoolean(7, account.isLoggedIn());
            stmt.setString(8, account.getUsername().get());

            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.error("Error saving user account", ex);
        }

        return false;
    }

    private void writeAccount(PreparedStatement stmt, Account account) throws SQLException {
        stmt.setString(1, account.getUsername().get().toLowerCase());
        stmt.setString(2, account.getUsername().get());
        stmt.setString(3, account.getPassword());
        stmt.setString(4, account.getIP().getHostAddress());

        stmt.setTimestamp(5, Timestamp.from(account.getLastLogin()));
        stmt.setString(6, account.getEmail().orElse(null));
    }

    @Override
    protected Optional<Account> parseLoadResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            String username = resultSet.getString(1);
            String password = resultSet.getString(2);

            InetAddress ip = parseAddress(resultSet.getString(3));
            Timestamp lastLogin = Optional.ofNullable(resultSet.getTimestamp(4)).orElse(new Timestamp(0));
            String email = resultSet.getString(5);

            UUID offlineUUID = Account.getOfflineUUID(username);
            return Optional.of(new Account(offlineUUID, username, password, ip, email, lastLogin.toInstant()));
        }

        return Optional.empty();
    }

    private InetAddress parseAddress(String hostName) {
        if (hostName == null || hostName.isEmpty()) {
            return null;
        }

        try {
            return InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
