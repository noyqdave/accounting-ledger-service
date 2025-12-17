package com.example.ledger.bdd.enabled;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring configuration for Cucumber tests with feature flags enabled.
 * This configuration uses the "test" profile which has all feature flags enabled.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfigurationEnabled {
}
