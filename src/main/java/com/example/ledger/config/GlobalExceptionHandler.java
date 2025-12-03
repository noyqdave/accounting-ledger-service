package com.example.ledger.config;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeatureFlagDisabledException.class)
    public ResponseEntity<ErrorResponse> handleFeatureFlagDisabled(FeatureFlagDisabledException ex) {
        ErrorResponse error = new ErrorResponse("Feature is disabled");
        return ResponseEntity.status(403).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid request format";
        if (ex.getMessage() != null && ex.getMessage().contains("TransactionType")) {
            errorMessage = "Invalid transaction type. Must be either EXPENSE or REVENUE";
        }
        ErrorResponse error = new ErrorResponse(errorMessage);
        return ResponseEntity.status(400).body(error);
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}