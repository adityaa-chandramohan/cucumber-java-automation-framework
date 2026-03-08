package com.automation.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Main JUnit test runner for all Cucumber scenarios.
 *
 * Tag filtering at runtime:
 *   mvn test -Dcucumber.filter.tags="@smoke"
 *   mvn test -Dcucumber.filter.tags="@regression"
 *   mvn test -Dcucumber.filter.tags="@posts and not @wip"
 *
 * Environment selection:
 *   mvn test -Denv=staging
 *   mvn test -P staging
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.automation.steps", "com.automation.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/cucumber-reports/cucumber.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags = "not @wip",
        publish = false
)
public class TestRunner {
}
