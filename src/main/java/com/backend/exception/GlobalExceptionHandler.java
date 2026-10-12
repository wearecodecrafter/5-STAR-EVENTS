package com.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(
            Exception ex, HttpStatus status) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("status", status.value());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerException(
            CustomerException ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PackageException.class)
    public ResponseEntity<Map<String, Object>> handlePackageException(
            PackageException ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST);
    }
}
