package com.github.games647.flexiblelogin.storage;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.SQLConfig;
import com.github.games647.flexiblelogin.config.SQLConfig.Type;
import com.github.games647.flexiblelogin.config.Settings;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.sql.SqlService;

public abstract class Database {

    private static final String SQL_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected final Logger logger;
    protected final SQLConfig.Type storageType;

    protected final Map<UUID, Account> cache = new ConcurrentHashMap<>();
    protected final DataSource dataSource;

    protected final String tableName;

    public Database(Logger logger, Settings settings, String tableName) throws SQLException {
        this.logger = logger;
        this.tableName = tableName;

        SQLConfig sqlConfig = settings.getGeneral().getSQL();
        Path configDir = settings.getConfigDir();
        this.storageType = sqlConfig.getType();

        String jdbcConnection = buildJDBCUrl(sqlConfig, configDir);
        this.dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(jdbcConnection);
    }

    private String buildJDBCUrl(SQLConfig sqlConfig, Path configDir) {
        String storagePath = sqlConfig.getPath().replace("%DIR%", configDir.normalize().toString());

        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(sqlConfig.getType().getJDBCId())
                .append("://");
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
                        .append("?useSSL").append('=').append(sqlConfig.isUseSSL());
                break;
            case H2:
            default:
                urlBuilder.append(storagePath).append(File.separatorChar).append("database");
                break;
        }

        return urlBuilder.toString();
    }

    public Optional<Account> getAccount(User player) {
        return Optional.ofNullable(cache.get(player.getUniqueId()));
    }

    public Optional<Account> loadAccount(User player) {
        Optional<Account> optAcc = loadAccount(player.getUniqueId());
        optAcc.ifPresent(account -> cache.put(player.getUniqueId(), account));
        return optAcc;
    }

    public Optional<Account> remove(User player) {
        return Optional.ofNullable(cache.remove(player.getUniqueId()));
    }

    public boolean isLoggedIn(User player) {
        return getAccount(player).map(Account::isLoggedIn).orElse(false);
    }

    public void addCache(UUID uniqueId, Account account) {
        cache.put(uniqueId, account);
    }

    public Optional<String> exists(String username, String nameColumn) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT " + nameColumn
                     + " FROM " + tableName + " WHERE " + nameColumn + "=?")) {
            stmt.setString(1, username);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString(1));
                }
            }
        } catch (SQLException sqlEx) {
            logger.error("Error deleting user account", sqlEx);
        }

        return Optional.empty();
    }

    public abstract boolean deleteAccount(String playerName);
    public abstract boolean deleteAccount(UUID uuid);

    public abstract Optional<String> exists(String username);
    public abstract Optional<Account> loadAccount(UUID uuid);
    public abstract Optional<Account> loadAccount(String playerName);
    public abstract boolean save(Account account);
    public abstract boolean createAccount(Account account);
    public abstract int getRegistrationsCount(InetAddress ip);
    public abstract Set<Account> getAccountsByIp(InetAddress ip);

    protected abstract Optional<Account> parseLoadResult(ResultSet resultSet) throws SQLException;

    protected Optional<Account> loadAccount(String sql, StatementConsumer idSetter) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            idSetter.accept(stmt);

            try (ResultSet resultSet = stmt.executeQuery()) {
                return parseLoadResult(resultSet);
            }
        } catch (SQLException sqlEx) {
            logger.error("Error loading account", sqlEx);
        }

        return Optional.empty();
    }

    protected boolean deleteAccount(String whereColumn, StatementConsumer idSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tableName + " WHERE "
                     + whereColumn +"=?")) {
            idSetter.accept(stmt);

            //min one account was found
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException sqlEx) {
            logger.error("Error deleting user account", sqlEx);
        }

        return false;
    }

    protected Instant parseTimestamp(ResultSet resultSet, int pos) throws SQLException {
        if (storageType == Type.SQLITE) {
            //workaround for SQLite that causes time parsing errors in combination with CURRENT_TIMESTAMP in SQL
            String timestamp = resultSet.getString(pos);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(SQL_TIME_FORMAT);
            return LocalDateTime.parse(timestamp, timeFormatter).toInstant(ZoneOffset.UTC);
        }

        return resultSet.getTimestamp(6).toInstant();
    }

    protected int getRegistrationsCount(InetAddress ip, StatementConsumer ipSerializer) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE IP=?")) {
            ipSerializer.accept(stmt);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException sqlEx) {
            logger.error("Error loading count of registrations", sqlEx);
        }

        return -1;
    }
    
    protected Set<Account> getAccountsByIp(InetAddress ip, StatementConsumer idSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE IP=?")) {
            idSetter.accept(stmt);

            ImmutableSet.Builder<Account> accountsBuilder = ImmutableSet.builder();
            try (ResultSet resultSet = stmt.executeQuery()) {
                do {
                    parseLoadResult(resultSet).ifPresent(accountsBuilder::add);
                } while(resultSet.next());
            }
            
            return accountsBuilder.build();
        } catch (SQLException sqlEx) {
            logger.error("Error getting accounts of player", sqlEx);
        }

        return Collections.emptySet();
    }

    public void close() {
        cache.clear();
    }

    public void createTable(FlexibleLogin plugin, Settings type) throws IOException, SQLException {}
}
