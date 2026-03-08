package com.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing a Post from the JSONPlaceholder /posts API.
 *
 * Field mapping:
 *   id     - Unique post identifier (auto-assigned, not sent on create)
 *   userId - ID of the user who authored the post
 *   title  - Post title (required)
 *   body   - Post body content (required)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {
    private Integer id;
    private Integer userId;
    private String title;
    private String body;
}
