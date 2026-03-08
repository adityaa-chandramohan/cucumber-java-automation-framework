package com.automation.context;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared scenario context for passing state between step definitions.
 * Injected via PicoContainer — one instance per scenario.
 *
 * Usage:
 *   - Store the last API response with setResponse()
 *   - Store arbitrary values with set()/get() for cross-step data sharing
 */
public class ScenarioContext {

    private Response lastResponse;
    private final Map<String, Object> contextData = new HashMap<>();

    public void setResponse(Response response) {
        this.lastResponse = response;
    }

    public Response getResponse() {
        return lastResponse;
    }

    public void set(String key, Object value) {
        contextData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) contextData.get(key);
    }

    public boolean contains(String key) {
        return contextData.containsKey(key);
    }

    public void clear() {
        lastResponse = null;
        contextData.clear();
    }
}
