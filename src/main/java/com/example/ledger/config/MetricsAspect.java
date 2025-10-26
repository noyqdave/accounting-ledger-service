package com.example.ledger.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(trackMetric)")
    public Object recordMetric(ProceedingJoinPoint pjp, TrackMetric trackMetric) throws Throwable {
        String name = trackMetric.value();

        meterRegistry.counter(name).increment();

        return pjp.proceed();
    }
}