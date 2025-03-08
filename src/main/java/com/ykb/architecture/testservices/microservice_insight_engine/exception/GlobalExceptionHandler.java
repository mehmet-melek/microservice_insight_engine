package com.ykb.architecture.testservices.microservice_insight_engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        // Log the exception
        ex.printStackTrace();
        
        return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 