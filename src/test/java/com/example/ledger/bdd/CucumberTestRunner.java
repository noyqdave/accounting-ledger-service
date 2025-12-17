package com.example.ledger.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {
        "com.example.ledger.bdd.enabled",  // Contains CucumberSpringConfigurationEnabled (uses enabled flags)
        "com.example.ledger.bdd.steps"     // Contains step definitions
    },
    plugin = {"pretty", "html:target/cucumber-reports/cucumber.html", "json:target/cucumber-reports/cucumber.json"},
    tags = "not @ff_disabled",  // Exclude disabled scenarios (they run in disabled runner)
    monochrome = true
)
public class CucumberTestRunner {
}
