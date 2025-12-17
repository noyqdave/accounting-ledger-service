package com.example.ledger.bdd.steps;

import com.example.ledger.config.FeatureFlagService;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

public class PropertySourceDebugHook {

    @Autowired
    private ConfigurableEnvironment env;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Before(order = 0)
    public void dumpFeatureFlagResolution() {
        String key = "feature.get-all-transactions.enabled";

        System.out.println("=== DEBUG feature flag resolution ===");
        System.out.println("Active profiles: " + String.join(",", env.getActiveProfiles()));
        System.out.println("Resolved value for " + key + " = " + env.getProperty(key));
        
        // Also check the actual bean value
        try {
            FeatureFlagService ffService = applicationContext.getBean(FeatureFlagService.class);
            System.out.println("FeatureFlagService.isEnabled('get-all-transactions') = " + 
                ffService.isEnabled("get-all-transactions"));
            
            // Check the FeatureFlagProperties bean
            com.example.ledger.config.FeatureFlagProperties props = 
                applicationContext.getBean(com.example.ledger.config.FeatureFlagProperties.class);
            System.out.println("FeatureFlagProperties bean exists: " + (props != null));
            System.out.println("FeatureFlagProperties.getEndpoints() = " + props.getEndpoints());
            System.out.println("Endpoint map size: " + (props.getEndpoints() != null ? props.getEndpoints().size() : 0));
            if (props.getEndpoints() != null && !props.getEndpoints().isEmpty()) {
                System.out.println("Endpoint map contents:");
                props.getEndpoints().forEach((k, v) -> System.out.println("  " + k + " -> " + v));
            }
            
            // Check the filter
            com.example.ledger.config.FeatureFlagFilter filter = 
                applicationContext.getBean(com.example.ledger.config.FeatureFlagFilter.class);
            System.out.println("FeatureFlagFilter bean exists: " + (filter != null));
        } catch (Exception e) {
            System.out.println("Could not access FeatureFlagService: " + e.getMessage());
            e.printStackTrace();
        }

        for (PropertySource<?> ps : env.getPropertySources()) {
            Object v = ps.getProperty(key);
            if (v != null) {
                System.out.println("  FOUND in PropertySource [" + ps.getName() + "] = " + v);
            }
        }
        System.out.println("=== END DEBUG ===");
    }
}
