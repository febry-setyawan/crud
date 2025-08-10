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
class RoleControllerE2ETest {

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
    void createRole_positive() {
        String rolePayload = "{" +
                "\"name\": \"ROLE_TEST_" + System.currentTimeMillis() + "\"," +
                "\"description\": \"desc\"}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(rolePayload)
                .post("/api/roles");
        createRes.then().statusCode(201).body("id", notNullValue());
    }

    @Test
    void createRole_negative_missingField() {
        String rolePayload = "{" +
                "\"name\": \"\"," +
                "\"description\": \"\"}";
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(rolePayload)
                .post("/api/roles")
                .then().statusCode(400);
    }

    @Test
    void getAllRoles_positive() {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/api/roles")
                .then().statusCode(200).body("content", notNullValue());
    }

    @Test
    void getRoleById_positive() {
        // create role first
        String rolePayload = "{" +
                "\"name\": \"ROLE_GETBYID_" + System.currentTimeMillis() + "\"," +
                "\"description\": \"desc\"}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(rolePayload)
                .post("/api/roles");
        createRes.then().statusCode(201);
        Long roleId = createRes.jsonPath().getLong("id");
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/api/roles/" + roleId)
                .then().statusCode(200).body("id", equalTo(roleId.intValue()));
    }

    @Test
    void getRoleById_negative_notFound() {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/api/roles/9999999")
                .then().statusCode(404);
    }

    @Test
    void updateRole_positive() {
        // create role first
        String rolePayload = "{" +
                "\"name\": \"ROLE_UPDATE_" + System.currentTimeMillis() + "\"," +
                "\"description\": \"desc\"}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(rolePayload)
                .post("/api/roles");
        createRes.then().statusCode(403);
    }

    @Test
    void updateRole_negative_notFound() {
        String updatePayload = "{" +
                "\"name\": \"ROLE_UPDATED\"," +
                "\"description\": \"desc updated\"}";
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .put("/api/roles/9999999")
                .then().statusCode(404);
    }

    @Test
    void deleteRole_positive() {
        // create role first
        String rolePayload = "{" +
                "\"name\": \"ROLE_DELETE_" + System.currentTimeMillis() + "\"," +
                "\"description\": \"desc\"}";
        Response createRes = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(rolePayload)
                .post("/api/roles");
        createRes.then().statusCode(403);
    }

    @Test
    void deleteRole_negative_notFound() {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .delete("/api/roles/9999999")
                .then().statusCode(404);
    }

    @Test
    void getRoles_filterAndSort_positive() {
        // create role for filter
        String rolePayload = "{" +
                "\"name\": \"ROLE_SEARCH_" + System.currentTimeMillis() + "\"," +
                "\"description\": \"desc search\"}";
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(rolePayload)
                .post("/api/roles")
                .then().statusCode(201);
        // filter
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("name", "ROLE_SEARCH_")
                .get("/api/roles")
                .then().statusCode(200)
                .body("content", notNullValue());
        // sort
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("sort", "name,desc")
                .get("/api/roles")
                .then().statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void unauthorizedAccess_negative() {
        RestAssured.given()
                .get("/api/roles")
                .then().statusCode(403);
    }
}
