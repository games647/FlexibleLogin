/*
 * The MIT License
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
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.games647.flexiblelogin;

import com.github.games647.flexiblelogin.config.SQLType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    private final FlexibleLogin plugin;

    public DatabaseMigration(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    public void createTable() throws SQLException {
        try (Connection con = plugin.getDatabase().getDataSource().getConnection()) {
            boolean tableExists = false;
            try {
                //check if the table already exists
                try (Statement statement = con.createStatement()) {
                    statement.execute("SELECT 1 FROM " + Database.USERS_TABLE);
                }

                tableExists = true;
            } catch (SQLException sqlEx) {
                plugin.getLogger().debug("Table doesn't exist", sqlEx);
            }

            if (!tableExists) {
                if (plugin.getConfigManager().getGeneral().getSQL().getType() == SQLType.SQLITE) {
                    try (Statement statement = con.createStatement()) {
                        statement.execute("CREATE TABLE " + Database.USERS_TABLE + " ( "
                                + "`UserID` INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "`UUID` BINARY(16) NOT NULL , "
                                + "`Username` VARCHAR , "
                                + "`Password` VARCHAR(64) NOT NULL , "
                                + "`IP` BINARY(32) NOT NULL , "
                                + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                                + "`Email` VARCHAR(64) DEFAULT NULL , "
                                + "`LoggedIn` BOOLEAN DEFAULT 0, "
                                + "UNIQUE (`UUID`) "
                                + ')');
                    }
                } else {
                    try (Statement stmt = con.createStatement()) {
                        stmt.execute("CREATE TABLE " + Database.USERS_TABLE + " ( "
                                + "`UserID` INT UNSIGNED NOT NULL AUTO_INCREMENT , "
                                + "`UUID` BINARY(16) NOT NULL , "
                                + "`Username` VARCHAR, "
                                + "`Password` VARCHAR(64) NOT NULL , "
                                + "`IP` BINARY(32) NOT NULL , "
                                + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                                + "`Email` VARCHAR(64) DEFAULT NULL , "
                                + "`LoggedIn` BOOLEAN DEFAULT 0, "
                                + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                                + ')');
                    }
                }
            }
        }
    }
}
