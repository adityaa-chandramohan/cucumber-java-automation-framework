package com.automation.clients;

import io.restassured.response.Response;

/**
 * API client for the /users resource on JSONPlaceholder.
 *
 * Base URL: https://jsonplaceholder.typicode.com
 * Endpoints:
 *   GET    /users           - Get all users
 *   GET    /users/{id}      - Get user by ID
 *   GET    /users/{id}/posts - Get all posts for a user
 *
 * Model: User { id, name, username, email, address, phone, website, company }
 */
public class UsersApiClient extends ApiClient {

    private static final String USERS_PATH = "/users";

    public Response getAllUsers() {
        log.info("Fetching all users");
        return get(USERS_PATH);
    }

    public Response getUserById(int userId) {
        log.info("Fetching user with id: {}", userId);
        return get(USERS_PATH + "/" + userId);
    }

    public Response getPostsForUser(int userId) {
        log.info("Fetching posts for user id: {}", userId);
        return get(USERS_PATH + "/" + userId + "/posts");
    }
}
