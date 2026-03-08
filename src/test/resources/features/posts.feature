@posts @regression
Feature: Posts API
  As an API consumer
  I want to interact with the Posts resource
  So that I can create, retrieve, update and delete posts

  Background:
    Given the posts API is available

  @smoke
  Scenario: Get all posts returns a non-empty list
    When I request all posts
    Then the response status code should be 200
    And the response should contain a list of posts

  @smoke
  Scenario: Get a specific post by ID
    When I request post with id 1
    Then the response status code should be 200
    And the post should have id 1
    And the post should have a non-null title

  Scenario: Get posts filtered by user ID returns only that user's posts
    When I request posts for user with id 1
    Then the response status code should be 200
    And the response should contain 10 posts
    And all posts should belong to user 1

  Scenario: Create a new post
    When I create a post with the following details:
      | userId | 1                              |
      | title  | My Automation Test Post        |
      | body   | This post was created by tests |
    Then the response status code should be 201
    And the created post should have a generated id
    And the created post title should match the request

  Scenario: Delete a post returns 200
    When I delete post with id 1
    Then the response status code should be 200

  Scenario: Patch a post title
    When I update post 1 title to "Updated Title"
    Then the response status code should be 200
    And the post title should be "Updated Title"

  @schema
  Scenario: Post response matches expected JSON schema
    When I request post with id 1
    Then the response status code should be 200
    And the response should match the post schema

  @negative
  Scenario: Request a non-existent post returns 404
    When I request post with id 9999
    Then the response status code should be 404
