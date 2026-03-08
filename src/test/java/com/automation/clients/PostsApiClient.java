package com.automation.clients;

import com.automation.models.Post;
import io.restassured.response.Response;

import java.util.Map;

/**
 * API client for the /posts resource on JSONPlaceholder.
 *
 * Base URL: https://jsonplaceholder.typicode.com
 * Endpoints:
 *   GET    /posts           - Get all posts
 *   GET    /posts/{id}      - Get post by ID
 *   GET    /posts?userId={} - Get posts filtered by userId
 *   POST   /posts           - Create a new post
 *   PUT    /posts/{id}      - Full update of a post
 *   PATCH  /posts/{id}      - Partial update of a post
 *   DELETE /posts/{id}      - Delete a post
 *
 * Model: Post { id, userId, title, body }
 */
public class PostsApiClient extends ApiClient {

    private static final String POSTS_PATH = "/posts";

    public Response getAllPosts() {
        log.info("Fetching all posts");
        return get(POSTS_PATH);
    }

    public Response getPostById(int postId) {
        log.info("Fetching post with id: {}", postId);
        return get(POSTS_PATH + "/" + postId);
    }

    public Response getPostsByUserId(int userId) {
        log.info("Fetching posts for userId: {}", userId);
        return get(POSTS_PATH, Map.of("userId", userId));
    }

    public Response createPost(Post post) {
        log.info("Creating post: {}", post);
        return post(POSTS_PATH, post);
    }

    public Response updatePost(int postId, Post post) {
        log.info("Updating post id: {}", postId);
        return put(POSTS_PATH + "/" + postId, post);
    }

    public Response patchPost(int postId, Map<String, Object> fields) {
        log.info("Patching post id: {} with fields: {}", postId, fields);
        return patch(POSTS_PATH + "/" + postId, fields);
    }

    public Response deletePost(int postId) {
        log.info("Deleting post id: {}", postId);
        return delete(POSTS_PATH + "/" + postId);
    }
}
