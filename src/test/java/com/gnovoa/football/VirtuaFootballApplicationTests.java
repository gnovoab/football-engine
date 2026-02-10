package com.gnovoa.football;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Basic integration tests */
@ActiveProfiles("integrationTest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class VirtuaFootballApplicationTests {

  @Autowired private RestTestClient restTestClient;

  @Test
  @DisplayName("Should verify health endpoint returns UP")
  void healthEndpointReturnsUp() {
    restTestClient
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("UP");
  }

  @Test
  @DisplayName("Should verify Swagger UI HTML page loads")
  void swaggerUiIsReachable() {
    restTestClient
        .get()
        .uri("/swagger-ui/index.html")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentTypeCompatibleWith("text/html")
        .expectBody(String.class)
        .consumeWith(
            result -> {
              String body = result.getResponseBody();
              assertThat(body).isNotNull();
              assertThat(body).containsIgnoringCase("swagger ui");
            });
  }

  @Test
  @DisplayName("Should verify OpenAPI JSON spec is available")
  void openApiSpecShouldBeAvailable() {
    restTestClient
        .get()
        .uri("/v3/api-docs")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.openapi")
        .exists()
        .jsonPath("$.info.title")
        .isNotEmpty();
  }

  @Test
  void apiDocsYamlOk() {

    restTestClient
        .get()
        .uri("/v3/api-docs.yaml")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType("application/vnd.oai.openapi") // Standard OpenAPI YAML content type
        .expectBody(String.class)
        .consumeWith(
            result -> {
              String yamlBody = result.getResponseBody();
              assertThat(yamlBody).isNotNull();
              // Check for key YAML markers
              assertThat(yamlBody).contains("openapi: 3.");
              assertThat(yamlBody).contains("paths:");
            });
  }
}
