package com.automation.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import org.hamcrest.Matcher;

/**
 * Utility for JSON schema validation.
 * Schema files live in src/test/resources/schemas/
 */
public final class JsonSchemaUtils {

    private JsonSchemaUtils() {}

    /**
     * Returns a matcher that validates a JSON response against a schema file.
     *
     * @param schemaFileName  filename under src/test/resources/schemas/ (e.g., "post-schema.json")
     */
    public static Matcher<?> matchesSchema(String schemaFileName) {
        return JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/" + schemaFileName);
    }
}
