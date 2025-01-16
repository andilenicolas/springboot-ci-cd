package com.example.project.service.authService;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.example.project.entity.User;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;
import com.example.project.feature.auth.requests.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.project.feature.auth.requests.RegisterRequest;
import com.example.project.repository.readonly.IReadOnlyRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService 
{
	@Autowired
	private final IReadOnlyRepository readOnlyRepository;
	
	// TODO: Add logic to register a new user (e.g., save user to DB, hash password)
	@Override
	public String register(RegisterRequest request) 
	{
		var dbUser = readOnlyRepository.findAllByCondition(
				User.class,
			    (cb, root) -> {
			        Predicate emailEquals = cb.equal(root.get("email"), request.getEmail());
			        return cb.and(emailEquals);
			    }
		);

		return "User registered successfully";
	}
	
	@Override
	public String login(LoginRequest request) {
		// TODO: Add logic to authenticate user (e.g., validate credentials, generate token)
		return "User logged in successfully";
	}
	
	@Override
	public void logout(String userId) {
		// TODO: Add logic to log out a user (e.g., invalidate tokens)
	}
}
