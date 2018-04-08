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
package com.github.games647.flexiblelogin.config.node;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class SQLConfig {

    @Setting(comment = "SQL server type. You can choose between H2, SQLite and MySQL/MariaDB. " +
            "If you use MySQL/MariaDB. Just write MySQL.")
    private Type type = Type.H2;

    @Setting(comment = "Path where the database is located." +
            " This can be a file path (H2/SQLite) or an IP/Domain (MySQL/MariaDB)")
    private String path = "%DIR%";

    @Setting(comment = "Port for MySQL/MariaDB connections")
    private int port = 3306;

    @Setting(comment = "Database name")
    private String database = "flexiblelogin";

    @Setting(comment = "Username to login the database system")
    private String username = "";

    @Setting(comment = "Password in order to login")
    private String password = "";

    @Setting(comment = "It's strongly recommended to enable SSL and setup a SSL certificate if the MySQL/MariaDB " +
            "server isn't running on the same machine")
    private boolean useSSL;

    @Setting(comment = "Compatibility with the Bukkit plugin AuthMeReloaded. Here you can specify the table name. If " +
            "this option is empty, compatibility will be disabled and FlexibleLogin's schema will be used.")
    private String authMeTable = "";

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public String getAuthMeTable() {
        return authMeTable;
    }

    /**
     * Sponge has support for all these three drivers
     */
    @ConfigSerializable
    public enum Type {

        MYSQL,

        MARIADB,

        SQLITE,

        H2;

        public String getJDBCId() {
            return name().toLowerCase();
        }
    }
}
