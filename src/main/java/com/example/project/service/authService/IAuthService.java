package com.example.project.service.authService;

import com.example.project.feature.auth.requests.LoginRequest;
import com.example.project.feature.auth.requests.RegisterRequest;

public interface IAuthService 
{
	String register(RegisterRequest request);
	String login(LoginRequest request);
	void logout(String userId);
}
