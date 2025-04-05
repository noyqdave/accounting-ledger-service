package com.example.ledger.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeatureFlagDisabledException.class)
    public ResponseEntity<Void> handleFeatureFlagDisabled(FeatureFlagDisabledException ex) {
        return ResponseEntity.status(403).build();
    }
}