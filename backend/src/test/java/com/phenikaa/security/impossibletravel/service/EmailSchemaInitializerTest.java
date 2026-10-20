package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.config.EmailSchemaInitializer;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class EmailSchemaInitializerTest {
    @Test void upgradesExistingDatabaseIdempotentlyWithoutChangingAccounts() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:migration;MODE=MySQL;DB_CLOSE_DELAY=-1");
        var jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE users (id BIGINT PRIMARY KEY, username VARCHAR(50))");
        jdbc.update("INSERT INTO users(id, username) VALUES (1, 'legacy_user')");
        var initializer = new EmailSchemaInitializer(source);
        initializer.afterPropertiesSet();
        initializer.afterPropertiesSet();
        assertEquals("legacy_user", jdbc.queryForObject("SELECT username FROM users WHERE id=1", String.class));
        assertNull(jdbc.queryForObject("SELECT email FROM users WHERE id=1", String.class));
        assertFalse(jdbc.queryForObject("SELECT email_verified FROM users WHERE id=1", Boolean.class));
        try (var connection = source.getConnection()) {
            var script = new ClassPathResource("db/migration/V3__email_otp.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM email_challenges", Integer.class));
    }
}
