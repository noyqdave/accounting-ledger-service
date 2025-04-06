package com.example.ledger.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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

    @Around("@annotation(com.example.ledger.config.TrackMetric)")
    public Object track(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        TrackMetric annotation = methodSignature.getMethod().getAnnotation(TrackMetric.class);

        String metricName = annotation.value();
        Counter counter = meterRegistry.counter(metricName);
        counter.increment();

        return joinPoint.proceed();
    }
}