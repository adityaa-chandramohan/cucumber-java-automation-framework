package com.automation.clients;

import com.automation.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Base API client providing a RestAssured-backed HTTP client.
 * All API-specific clients should extend this class.
 *
 * Usage pattern:
 *   - Extend ApiClient for each service (e.g., PostsApiClient, UsersApiClient)
 *   - Use the protected methods get(), post(), put(), patch(), delete()
 *   - Override buildRequestSpec() to add service-specific headers (auth tokens, etc.)
 */
public abstract class ApiClient {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ConfigManager config = ConfigManager.getInstance();
    protected RequestSpecification requestSpec;

    protected ApiClient() {
        requestSpec = buildRequestSpec();
    }

    protected RequestSpecification buildRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    protected Response get(String path) {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(path)
                .then()
                .log().all()
                .extract().response();
    }

    protected Response get(String path, Map<String, Object> queryParams) {
        return RestAssured.given()
                .spec(requestSpec)
                .queryParams(queryParams)
                .when()
                .get(path)
                .then()
                .log().all()
                .extract().response();
    }

    protected Response post(String path, Object body) {
        return RestAssured.given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(path)
                .then()
                .log().all()
                .extract().response();
    }

    protected Response put(String path, Object body) {
        return RestAssured.given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(path)
                .then()
                .log().all()
                .extract().response();
    }

    protected Response patch(String path, Object body) {
        return RestAssured.given()
                .spec(requestSpec)
                .body(body)
                .when()
                .patch(path)
                .then()
                .log().all()
                .extract().response();
    }

    protected Response delete(String path) {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .delete(path)
                .then()
                .log().all()
                .extract().response();
    }
}
