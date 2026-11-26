package com.milki.mimi.platform;

import com.milki.mimi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
public class ErrorHandlingIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void shouldReturn404WithoutInternalDetailsWhenUnknownEndpoints() {
        restTestClient.get()
                .uri("/api/v1/nonexistent")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("The requested resource was not found");
    }

    @Test
    void shouldReturn405WithoutInternalDetailsWhenUnsupportedEndpoints() {
        restTestClient.delete()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody()
                .jsonPath("$.status").isEqualTo(405)
                .jsonPath("$.detail").isEqualTo("Http method 'DELETE' is not supported for this endpoint.");
    }
}
