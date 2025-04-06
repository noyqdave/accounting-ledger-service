package com.example.ledger.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
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