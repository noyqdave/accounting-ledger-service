package com.example.ledger.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FeatureFlagAspect {

    private final FeatureFlags featureFlags;

    public FeatureFlagAspect(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Around("@annotation(com.example.ledger.config.FeatureEnabled)")
    public Object checkFeatureEnabled(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        FeatureEnabled annotation = signature.getMethod().getAnnotation(FeatureEnabled.class);

        String featureName = annotation.value();
        boolean enabled = switch (featureName) {
            case "create-transaction" -> featureFlags.isCreateTransactionEnabled();
            case "get-all-transactions" -> featureFlags.isGetAllTransactionsEnabled();
            default -> throw new IllegalArgumentException("Unknown feature flag: " + featureName);
        };

        if (!enabled) {
            throw new FeatureFlagDisabledException("Feature '" + featureName + "' is disabled");
        }

        return joinPoint.proceed();
    }
}