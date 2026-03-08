@users @regression
Feature: Users API
  As an API consumer
  I want to retrieve user information
  So that I can verify user data and relationships

  @smoke
  Scenario: Get all posts returns a non-empty list
    When I request all posts
    Then the response status code should be 200
    And the response should contain a list of posts

  Scenario: Get posts filtered by user returns that user's posts
    When I request posts for user with id 1
    Then the response status code should be 200
    And all posts should belong to user 1
