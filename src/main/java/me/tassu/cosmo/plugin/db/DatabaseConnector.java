/*
 * The MIT License
 *
 * Copyright © 2018 Tassu <hello@tassu.me>
 * Copyright © 2018 Contributors
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

package me.tassu.cosmo.plugin.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.val;
import me.tassu.cosmo.plugin.cfg.CosmoConfig;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class DatabaseConnector {

    @Inject private Logger logger;
    @Inject private CosmoConfig cosmoConfig;

    private SqlService sql;
    private DataSource dataSource;

    private DataSource getDataSource() throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).orElseThrow(NullPointerException::new);
        }

        if (dataSource == null) {
            dataSource = sql.getDataSource(sql.getConnectionUrlFromAlias(cosmoConfig.getConfig().getDatabase()
                    .getDatabase()).orElseThrow(() -> new IllegalArgumentException("Invalid database provided")));
        }

        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        val connection = this.getDataSource().getConnection();

        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool.");
        }

        return connection;
    }

    public void connect() {
        if (dataSource != null) throw new IllegalStateException("Already connected.");

        // getConnection() opens the connection
        val start = System.currentTimeMillis();
        try (Statement s = getConnection().createStatement()) {
            s.execute("/* ping */ SELECT 1");
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed, check your database credentials. ", e);
        }

        val duration = System.currentTimeMillis() - start;

        logger.info(String.format("Connected to database. Ping: %dms", duration));
    }

    public void checkSchema() {
        val dbConfig = cosmoConfig.getConfig().getDatabase();

        try (Statement s = getConnection().createStatement()) {
            try (ResultSet table = s.executeQuery("SHOW TABLES LIKE '" +
                    dbConfig.getTables().getUserDataTable() + "';")) {
                if (!table.next()) {
                    s.execute("CREATE TABLE " + dbConfig.getTables().getUserDataTable()
                            + " (`uuid` VARCHAR(36) PRIMARY KEY NOT NULL," +
                            " `unlocked_cosmetics` TEXT NOT NULL);");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
