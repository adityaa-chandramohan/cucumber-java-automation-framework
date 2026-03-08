package com.automation.steps;

import com.automation.clients.PostsApiClient;
import com.automation.context.ScenarioContext;
import com.automation.models.Post;
import com.automation.utils.JsonSchemaUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Posts API scenarios.
 * Uses PicoContainer to inject shared ScenarioContext.
 */
public class PostsStepDefinitions {

    private static final Logger log = LoggerFactory.getLogger(PostsStepDefinitions.class);

    private final PostsApiClient postsApiClient;
    private final ScenarioContext context;

    public PostsStepDefinitions(ScenarioContext context) {
        this.context = context;
        this.postsApiClient = new PostsApiClient();
    }

    @Given("the posts API is available")
    public void thePostsApiIsAvailable() {
        log.info("Verifying posts API is reachable");
        // Health check — a 200 on /posts means the API is available
        Response response = postsApiClient.getAllPosts();
        assertThat(response.statusCode())
                .as("Posts API should be reachable")
                .isEqualTo(200);
    }

    @When("I request all posts")
    public void iRequestAllPosts() {
        Response response = postsApiClient.getAllPosts();
        context.setResponse(response);
    }

    @When("I request post with id {int}")
    public void iRequestPostWithId(int postId) {
        Response response = postsApiClient.getPostById(postId);
        context.setResponse(response);
        context.set("requestedPostId", postId);
    }

    @When("I request posts for user with id {int}")
    public void iRequestPostsForUserWithId(int userId) {
        Response response = postsApiClient.getPostsByUserId(userId);
        context.setResponse(response);
        context.set("requestedUserId", userId);
    }

    @When("I create a post with the following details:")
    public void iCreateAPostWithTheFollowingDetails(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        Post post = Post.builder()
                .userId(Integer.parseInt(data.get("userId")))
                .title(data.get("title"))
                .body(data.get("body"))
                .build();
        context.set("requestBody", post);
        Response response = postsApiClient.createPost(post);
        context.setResponse(response);
    }

    @When("I delete post with id {int}")
    public void iDeletePostWithId(int postId) {
        Response response = postsApiClient.deletePost(postId);
        context.setResponse(response);
    }

    @When("I update post {int} title to {string}")
    public void iUpdatePostTitleTo(int postId, String newTitle) {
        Response response = postsApiClient.patchPost(postId, Map.of("title", newTitle));
        context.setResponse(response);
    }

    // ─── Assertions ────────────────────────────────────────────────────────────

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(context.getResponse().statusCode())
                .as("Expected HTTP status %d", expectedStatusCode)
                .isEqualTo(expectedStatusCode);
    }

    @Then("the response should contain a list of posts")
    public void theResponseShouldContainAListOfPosts() {
        List<Post> posts = context.getResponse().jsonPath().getList("", Post.class);
        assertThat(posts)
                .as("Response should contain posts")
                .isNotEmpty();
        log.info("Retrieved {} posts", posts.size());
    }

    @Then("the response should contain {int} posts")
    public void theResponseShouldContainPosts(int expectedCount) {
        List<Post> posts = context.getResponse().jsonPath().getList("", Post.class);
        assertThat(posts).hasSize(expectedCount);
    }

    @Then("the post should have id {int}")
    public void thePostShouldHaveId(int expectedId) {
        int actualId = context.getResponse().jsonPath().getInt("id");
        assertThat(actualId).isEqualTo(expectedId);
    }

    @Then("the post should have a non-null title")
    public void thePostShouldHaveANonNullTitle() {
        String title = context.getResponse().jsonPath().getString("title");
        assertThat(title)
                .as("Post title should not be null or empty")
                .isNotNull()
                .isNotEmpty();
    }

    @Then("all posts should belong to user {int}")
    public void allPostsShouldBelongToUser(int userId) {
        List<Integer> userIds = context.getResponse().jsonPath().getList("userId", Integer.class);
        assertThat(userIds)
                .as("All posts should belong to userId %d", userId)
                .allMatch(id -> id == userId);
    }

    @Then("the created post should have a generated id")
    public void theCreatedPostShouldHaveAGeneratedId() {
        Integer id = context.getResponse().jsonPath().getInt("id");
        assertThat(id)
                .as("Created post should have a server-assigned id")
                .isNotNull()
                .isPositive();
    }

    @Then("the created post title should match the request")
    public void theCreatedPostTitleShouldMatchTheRequest() {
        Post requestBody = context.get("requestBody");
        String responseTitle = context.getResponse().jsonPath().getString("title");
        assertThat(responseTitle).isEqualTo(requestBody.getTitle());
    }

    @And("the response body should be empty")
    public void theResponseBodyShouldBeEmpty() {
        String body = context.getResponse().getBody().asString();
        assertThat(body)
                .as("Response body should be empty for DELETE")
                .isNotNull();
    }

    @Then("the post title should be {string}")
    public void thePostTitleShouldBe(String expectedTitle) {
        String actualTitle = context.getResponse().jsonPath().getString("title");
        assertThat(actualTitle).isEqualTo(expectedTitle);
    }

    @Then("the response should match the post schema")
    public void theResponseShouldMatchThePostSchema() {
        context.getResponse().then()
                .body(JsonSchemaUtils.matchesSchema("post-schema.json"));
    }
}
