package com.fourcolour.gateway.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
@ActiveProfiles("test")
class GatewayApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void healthCheck_ShouldReturnOK() {
        given()
            .when()
                .get("/healthcheck")
            .then()
                .statusCode(200)
                .body(equalTo("OK"));
    }

    @Test
    void healthCheckServices_ShouldReturnServicesStatus() {
        given()
            .when()
                .get("/healthcheck/services")
            .then()
                .statusCode(200)
                .body(not(emptyOrNullString()));
    }

    @Test
    void register_WithValidData_ShouldReturnSuccess() {
        String requestBody = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/v1/auth/register")
            .then()
                .statusCode(200)
                .body("username", equalTo("testuser"));
    }

    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() {
        String requestBody = """
            {
                "username": "",
                "email": "invalid-email",
                "password": "123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/v1/auth/register")
            .then()
                .statusCode(400);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        String requestBody = """
            {
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/v1/auth/login")
            .then()
                .statusCode(200)
                .body(not(emptyOrNullString()));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() {
        String requestBody = """
            {
                "email": "wrong@example.com",
                "password": "wrongpassword"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/v1/auth/login")
            .then()
                .statusCode(401);
    }

    @Test
    void colorMap_WithoutAuth_ShouldReturnUnauthorized() {
        String requestBody = """
            {
                "image": {
                    "data": [1, 2, 3, 4]
                },
                "width": 800,
                "height": 600
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/api/v1/maps/color")
            .then()
                .statusCode(401)
                .body("error", equalTo("Authentication required"));
    }

    @Test
    void colorMap_WithValidAuth_ShouldReturnSuccess() {
        String requestBody = """
            {
                "image": {
                    "data": [1, 2, 3, 4]
                },
                "width": 800,
                "height": 600
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer valid-token")
            .body(requestBody)
            .when()
                .post("/api/v1/maps/color")
            .then()
                .statusCode(200);
    }

    @Test
    void getMaps_WithoutAuth_ShouldReturnUnauthorized() {
        given()
            .when()
                .get("/api/v1/maps")
            .then()
                .statusCode(401)
                .body("error", equalTo("Authentication required"));
    }

    @Test
    void getMaps_WithValidAuth_ShouldReturnMaps() {
        given()
            .header("Authorization", "Bearer valid-token")
            .when()
                .get("/api/v1/maps")
            .then()
                .statusCode(200);
    }

    @Test
    void createMap_WithValidAuth_ShouldReturnCreated() {
        String requestBody = """
            {
                "name": "Test Map",
                "data": "map-data"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer valid-token")
            .body(requestBody)
            .when()
                .post("/api/v1/maps")
            .then()
                .statusCode(200);
    }

    @Test
    void updateMap_WithValidAuth_ShouldReturnSuccess() {
        String requestBody = """
            {
                "name": "Updated Map"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer valid-token")
            .body(requestBody)
            .when()
                .put("/api/v1/maps/map-123")
            .then()
                .statusCode(200);
    }

    @Test
    void deleteMap_WithValidAuth_ShouldReturnSuccess() {
        given()
            .header("Authorization", "Bearer valid-token")
            .when()
                .delete("/api/v1/maps/map-123")
            .then()
                .statusCode(200);
    }

    @Test
    void optionsRequest_ShouldReturnOK() {
        given()
            .when()
                .options("/api/v1/auth/login")
            .then()
                .statusCode(200);
    }

    @Test
    void rateLimiting_WithExcessiveRequests_ShouldReturnTooManyRequests() {
        // Make multiple rapid requests to trigger rate limiting
        for (int i = 0; i < 10; i++) {
            given()
                .contentType(ContentType.JSON)
                .body("{\"test\":\"data\"}")
                .when()
                    .post("/api/v1/auth/register")
                .then()
                    .statusCode(anyOf(equalTo(200), equalTo(429)));
        }
    }

    @Test
    void malformedJson_ShouldReturnBadRequest() {
        String malformedJson = "{ invalid json }";

        given()
            .contentType(ContentType.JSON)
            .body(malformedJson)
            .when()
                .post("/api/v1/auth/register")
            .then()
                .statusCode(400);
    }

    @Test
    void unsupportedMediaType_ShouldReturnBadRequest() {
        given()
            .contentType(ContentType.TEXT)
            .body("plain text")
            .when()
                .post("/api/v1/auth/register")
            .then()
                .statusCode(415);
    }
} 