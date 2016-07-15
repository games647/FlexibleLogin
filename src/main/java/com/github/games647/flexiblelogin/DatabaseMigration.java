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

    private static final String OLD_TABLE_NAME = "users";

    private final FlexibleLogin plugin;

    public DatabaseMigration(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    public void createTable() throws SQLException {
        Connection conn = null;
        try {
            conn = plugin.getDatabase().getConnection();

            boolean tableExists = false;
            try {
                //check if the table already exists
                Statement statement = conn.createStatement();
                statement.execute("SELECT 1 FROM " + Database.USERS_TABLE);
                statement.close();

                tableExists = true;
            } catch (SQLException sqlEx) {
                plugin.getLogger().debug("Table doesn't exist", sqlEx);
            }

            if (!tableExists) {
                if (plugin.getConfigManager().getConfig().getSqlConfiguration().getType() == SQLType.SQLITE) {
                    Statement statement = conn.createStatement();
                    statement.execute("CREATE TABLE " + Database.USERS_TABLE + " ( "
                            + "`UserID` INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "`UUID` BINARY(16) NOT NULL , "
                            + "`Username` VARCHAR(32) NOT NULL , "
                            + "`Password` VARCHAR(64) NOT NULL , "
                            + "`IP` BINARY(32) NOT NULL , "
                            + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                            + "`Email` VARCHAR(64) DEFAULT NULL , "
                            + "`LoggedIn` BOOLEAN DEFAULT 0, "
                            + "UNIQUE (`UUID`) "
                            + ")");
                    statement.close();
                } else {
                    Statement statement = conn.createStatement();
                    statement.execute("CREATE TABLE " + Database.USERS_TABLE + " ( "
                            + "`UserID` INT UNSIGNED NOT NULL AUTO_INCREMENT , "
                            + "`UUID` BINARY(16) NOT NULL , "
                            + "`Username` VARCHAR(32) NOT NULL , "
                            + "`Password` VARCHAR(64) NOT NULL , "
                            + "`IP` BINARY(32) NOT NULL , "
                            + "`LastLogin` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , "
                            + "`Email` VARCHAR(64) DEFAULT NULL , "
                            + "`LoggedIn` BOOLEAN DEFAULT 0, "
                            + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                            + ")");
                    statement.close();
            	}
            }
        } finally {
            plugin.getDatabase().closeQuietly(conn);
        }
    }

    public void migrateName() {
        Connection conn = null;
        try {
            conn = plugin.getDatabase().getConnection();

            boolean tableExists = false;
            try {
                //check if the table already exists
                Statement statement = conn.createStatement();
                statement.execute("SELECT 1 FROM " + OLD_TABLE_NAME);
                statement.close();

                tableExists = true;
            } catch (SQLException sqlEx) {
                //Old Table doesn't exist
            }

            if (tableExists) {
                Statement statement = conn.createStatement();
                statement.execute("SELECT 1 FROM " + OLD_TABLE_NAME);

                //if no error happens the table exists
                statement.execute("INSERT INTO " + Database.USERS_TABLE + " SELECT *, 0 FROM " + OLD_TABLE_NAME);
                statement.execute("DROP TABLE " + OLD_TABLE_NAME);
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Error migrating database", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    //ignore
                }
            }
        }
    }
}
