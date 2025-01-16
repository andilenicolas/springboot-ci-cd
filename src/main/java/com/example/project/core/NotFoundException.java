package com.example.project.core;

import java.util.List;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException 
{
	private static final long serialVersionUID = 3715924044388556654L;

	public NotFoundException(String message) {
		super(message, HttpStatus.NOT_FOUND);
	}

	public NotFoundException(String message, List<String> errors) {
		super(message, errors, HttpStatus.NOT_FOUND);
	}

}



