package com.automation.hooks;

import com.automation.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global Cucumber hooks for setup and teardown.
 * Injected with ScenarioContext via PicoContainer.
 */
public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);
    private final ScenarioContext context;

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("═══════════════════════════════════════════════════════");
        log.info("STARTING: {}", scenario.getName());
        log.info("Tags: {}", scenario.getSourceTagNames());
        log.info("═══════════════════════════════════════════════════════");
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed() && context.getResponse() != null) {
            log.error("Scenario FAILED. Last response body: {}",
                    context.getResponse().getBody().asPrettyString());
        }
        context.clear();
        log.info("═══════════════════════════════════════════════════════");
        log.info("FINISHED [{}]: {}", scenario.getStatus(), scenario.getName());
        log.info("═══════════════════════════════════════════════════════");
    }
}
