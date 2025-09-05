package com.example.ledger.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/create-transaction-feature-disabled.feature",
    glue = "com.example.ledger.bdd",
    plugin = {"pretty", "html:target/cucumber-reports-feature-disabled/cucumber.html", "json:target/cucumber-reports-feature-disabled/cucumber.json"},
    monochrome = true
)
public class CucumberTestRunnerFeatureDisabled {
}
