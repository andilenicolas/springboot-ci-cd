package com.example.project.core;

import java.util.List;

import org.springframework.http.HttpStatus;

public class AppException extends Exception 
{
	private static final long serialVersionUID = 2213114679735045096L;
	
	private final HttpStatus status;
    private final String message;
	private List<String> errors;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.errors = null;
        this.status = status;
        this.message = message;
    }
    
    public AppException(String message, List<String> errors, HttpStatus status) {
        super(message);
        this.errors = errors;
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
