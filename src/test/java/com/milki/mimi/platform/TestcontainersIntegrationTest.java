package com.milki.mimi.platform;

import com.milki.mimi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class TestcontainersIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void runsAgainstIsolatedPostgres17WithFlywayApplied() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();

            // Verify isolated container port (not default localhost:5432)
            assertThat(jdbcUrl).doesNotContain("localhost:5432/");
            // Verify PostgreSQL 17
            assertThat(majorVersion).isEqualTo(17);
        }

        // Verify Flyway ran against the test container
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
                Integer.class
        );
        assertThat(appliedMigrations).isGreaterThan(0);
    }
}
