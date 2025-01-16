package com.example.project.feature.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.project.service.authService.IAuthService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.example.project.feature.auth.requests.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.project.feature.auth.requests.RegisterRequest;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController 
{
	@Autowired
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) 
    {
        String message = authService.register(request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    } 

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) 
    {
        String message = authService.login(request);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<Void> logout(@PathVariable @NotBlank String userId) {
        authService.logout(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
