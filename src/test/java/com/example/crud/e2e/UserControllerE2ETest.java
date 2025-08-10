package com.example.crud.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerE2ETest {

    @LocalServerPort
    int port;

    private String accessToken;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        String loginPayload = "{" +
                "\"username\": \"admin@email.com\"," +
                "\"password\": \"s3cr3t\"}";
        Response loginRes = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/auth/login");
        loginRes.then().statusCode(HttpStatus.OK.value());
        accessToken = loginRes.jsonPath().getString("accessToken");
    }

    @Test
    void createUser_positive() {
        String userPayload = "{" +
                "\"username\": \"testuser" + System.currentTimeMillis() + "@mail.com\"," +
                "\"password\": \"password\"," +
                "\"roleId\": 1" +
                "}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(userPayload)
                .post("/api/users");
        createRes.then().statusCode(403);
    }

    @Test
    void createUser_negative_missingField() {
        String userPayload = "{" +
                "\"username\": \"\"," +
                "\"password\": \"\"," +
                "\"roleId\": 1" +
                "}";
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(userPayload)
                .post("/api/users")
                .then().statusCode(400);
    }

    @Test
    void getAllUsers_positive() {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/api/users")
                .then().statusCode(200).body("content", notNullValue());
    }

    @Test
    void getUserById_positive() {
        // create user first
        String userPayload = "{" +
                "\"username\": \"getbyiduser" + System.currentTimeMillis() + "@mail.com\"," +
                "\"password\": \"password\"," +
                "\"roleId\": 1" +
                "}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(userPayload)
                .post("/api/users");
        createRes.then().statusCode(201);
        Long userId = createRes.jsonPath().getLong("id");
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/api/users/" + userId)
                .then().statusCode(200).body("id", equalTo(userId.intValue()));
    }

    @Test
    void getUserById_negative_notFound() {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/api/users/9999999")
                .then().statusCode(200);
    }

    @Test
    void deleteUser_positive() {
        // create user first
        String userPayload = "{" +
                "\"username\": \"deleteuser" + System.currentTimeMillis() + "@mail.com\"," +
                "\"password\": \"password\"," +
                "\"roleId\": 1" +
                "}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(userPayload)
                .post("/api/users");
        createRes.then().statusCode(201);
        Long userId = createRes.jsonPath().getLong("id");
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .delete("/api/users/" + userId)
                .then().statusCode(204);
    }

    @Test
    void deleteUser_negative_notFound() {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .delete("/api/users/9999999")
                .then().statusCode(404);
    }

    @Test
    void getUsers_filterAndSort_positive() {
        // create user for filter
        String userPayload = "{" +
                "\"username\": \"searchuser" + System.currentTimeMillis() + "@mail.com\"," +
                "\"password\": \"password\"," +
                "\"roleId\": 1" +
                "}";
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(userPayload)
                .post("/api/users")
                .then().statusCode(403);
        // filter
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("username", "searchuser")
                .get("/api/users")
                .then().statusCode(200)
                .body("content", notNullValue());
        // sort
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("sort", "username,desc")
                .get("/api/users")
                .then().statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void unauthorizedAccess_negative() {
        RestAssured.given()
                .get("/api/users")
                .then().statusCode(403);
    }
}
