package com.example.ledger.bdd.enabled;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Cucumber test runner for scenarios with feature flags enabled.
 * This runner excludes scenarios tagged with @ff_disabled.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {
        "com.example.ledger.bdd.enabled",  // Contains CucumberSpringConfigurationEnabled
        "com.example.ledger.bdd.steps"     // Shared step definitions
    },
    plugin = {"pretty", "html:target/cucumber-reports/cucumber.html", "json:target/cucumber-reports/cucumber.json"},
    tags = "not @ff_disabled",
    monochrome = true
)
public class CucumberTestRunnerEnabled {
}
