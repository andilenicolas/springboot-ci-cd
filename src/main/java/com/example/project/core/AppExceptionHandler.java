package com.example.project.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
public class AppExceptionHandler 
{  
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleException(Exception ex) {
    	var error = Response.error(ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }
	
	@ExceptionHandler(AppException.class)
    public ResponseEntity<Response<List<String>>> handleAppException(AppException ex) {
    	var error = Response.error(ex.getMessage(), ex.getErrors(), ex.getStatus());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    public  ResponseEntity<Response<List<String>>> handleNotFoundException(NotFoundException ex) {
    	var error = Response.error(ex.getMessage(), ex.getErrors(), ex.getStatus());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        var error = Response.error("Validation errors", errors, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, error.getStatus());
    }

}

