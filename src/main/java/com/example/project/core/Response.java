package com.example.project.core;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class Response<T> 
{	
	private T data;
    private String message;
    private boolean isSuccess;
    private HttpStatus status;
    private LocalDateTime timestamp;
    
    public Response(String message, T data, HttpStatus status, boolean isSuccess) 
    {
        this.data = data;
        this.status = status;
        this.message = message;
        this.isSuccess = isSuccess;
        this.timestamp = LocalDateTime.now();
    }  
    
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(message, data, HttpStatus.OK, true);
    }

    public static <T> Response<T> success(String message, T data, HttpStatus status) {
        return new Response<>(message, data, status, true);
    }
    
    public static <T> Response<T> error(String message) {
        return new Response<>(message, null, HttpStatus.INTERNAL_SERVER_ERROR, false);
    }
    
    public static <T> Response<T> error(String message, HttpStatus status) {
        return new Response<>(message, null, status, false);
    }
    
    public static <T> Response<T> error(String message, T data,  HttpStatus status) {
        return new Response<>(message, data, status, false);
    }
}
