package com.example.crud.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerE2ETest {
            
    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void login_positive() {
        String loginPayload = "{" +
                "\"username\": \"admin@email.com\"," +
                "\"password\": \"s3cr3t\"}";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/auth/login")
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void login_negative_wrongPassword() {
        String loginPayload = "{" +
                "\"username\": \"admin@email.com\"," +
                "\"password\": \"wrongpass\"}";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/auth/login")
                .then().statusCode(403);
    }

    @Test
    void refreshToken_positive() {
        String loginPayload = "{" +
                "\"username\": \"admin@email.com\"," +
                "\"password\": \"s3cr3t\"}";
        Response loginRes = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/auth/login");
        loginRes.then().statusCode(200);
        String refreshToken = loginRes.jsonPath().getString("refreshToken");
        String refreshPayload = "{" +
                "\"refreshToken\": \"" + refreshToken + "\"}";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(refreshPayload)
                .post("/api/auth/refresh")
                .then().statusCode(200)
                .body("accessToken", notNullValue());
    }

    @Test
    void refreshToken_negative_invalidToken() {
        String refreshPayload = "{" +
                "\"refreshToken\": \"invalidtoken\"}";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(refreshPayload)
                .post("/api/auth/refresh")
                .then().statusCode(401);
    }

    @Test
    void logout_positive() {
        String loginPayload = "{" +
                "\"username\": \"admin@email.com\"," +
                "\"password\": \"s3cr3t\"}";
        Response loginRes = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/auth/login");
        loginRes.then().statusCode(200);
        String refreshToken = loginRes.jsonPath().getString("refreshToken");
        String logoutPayload = "{" +
                "\"refreshToken\": \"" + refreshToken + "\"}";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(logoutPayload)
                .post("/api/auth/logout")
                .then().statusCode(200);
    }

    @Test
    void logout_negative_invalidToken() {
        String logoutPayload = "{" +
                "\"refreshToken\": \"invalidtoken\"}";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(logoutPayload)
                .post("/api/auth/logout")
                .then().statusCode(200);
    }
}
