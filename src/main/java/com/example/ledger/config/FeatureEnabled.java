package com.example.ledger.config;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeatureEnabled {
    String value(); // the name of the feature flag
}
