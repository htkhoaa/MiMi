package com.milki.mimi.platform;

import com.milki.mimi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class ApplicationStartupIntegrationTest {

    @Autowired
    private PostgreSQLContainer postgreSQLContainer;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applicationContextLoadsAndDatabaseIsReady() throws SQLException {
        // Verify Spring application context loaded cleanly
        assertThat(applicationContext).isNotNull();
        assertThat(postgreSQLContainer.isRunning()).isTrue();

        // Verify database connection is established
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()
                    .equalsIgnoreCase("PostgreSQL")).isTrue();
        }

        //  Verify Flyway ran and created is migration tracking table.
        Boolean flywayTableExists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables
                    WHERE table_schema = 'public'
                    AND table_name = 'flyway_schema_history'
                )
                """, Boolean.class
        );
        assertThat(flywayTableExists).isTrue();

        // Verify at least one migration succeeded with no failed migrations.
        Integer successfulMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true", Integer.class
        );
        assertThat(successfulMigrations).isGreaterThan(0);
    }
}
