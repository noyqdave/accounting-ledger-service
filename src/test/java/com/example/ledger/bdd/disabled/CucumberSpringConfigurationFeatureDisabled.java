package com.example.ledger.bdd.disabled;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring configuration for Cucumber tests with feature flags disabled.
 * This configuration uses both "test" and "ff-disabled" profiles.
 * The "ff-disabled" profile will override feature flag settings to disable them.
 * 
 * IMPORTANT: This class must be in a package that is ONLY included in the glue
 * for CucumberTestRunnerFeatureDisabled. It should NOT be visible to other runners.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "ff-disabled"})
public class CucumberSpringConfigurationFeatureDisabled {
    // This class is intentionally in a separate package (bdd.disabled)
    // to ensure it's only found by CucumberTestRunnerFeatureDisabled
}
