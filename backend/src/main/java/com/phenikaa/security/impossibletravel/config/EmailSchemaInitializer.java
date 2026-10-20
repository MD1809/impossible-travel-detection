package com.phenikaa.security.impossibletravel.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

/** Adds nullable email columns to existing installations without changing accounts. */
@Component
@DependsOnDatabaseInitialization
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.email.migrate", havingValue = "true", matchIfMissing = true)
public class EmailSchemaInitializer implements InitializingBean {
    private final DataSource dataSource;
    public EmailSchemaInitializer(DataSource dataSource) { this.dataSource = dataSource; }
    @Override public void afterPropertiesSet() throws Exception {
        try (var connection = dataSource.getConnection()) {
            for (String[] column : new String[][] {{"email", "VARCHAR(254) NULL"}, {"email_verified", "BOOLEAN NOT NULL DEFAULT FALSE"}}) {
                boolean exists = false;
                try (var columns = connection.getMetaData().getColumns(connection.getCatalog(), null, null, null)) {
                    while (columns.next()) {
                        if ("users".equalsIgnoreCase(columns.getString("TABLE_NAME")) && column[0].equalsIgnoreCase(columns.getString("COLUMN_NAME"))) { exists = true; break; }
                    }
                }
                if (!exists) try (var statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE users ADD COLUMN " + column[0] + " " + column[1]);
                }
            }
        }
    }
}
