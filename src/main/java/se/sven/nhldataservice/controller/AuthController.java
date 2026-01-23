package se.sven.nhldataservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import se.sven.nhldataservice.dto.LoginRequest;
import se.sven.nhldataservice.dto.LoginResponse;
import se.sven.nhldataservice.dto.RegisterRequest;
import se.sven.nhldataservice.dto.UserResponse;
import se.sven.nhldataservice.service.AuthService;
import se.sven.nhldataservice.util.JwtUtil;

/**
 * Authentication controller for JWT token-based login and registration.
 */
@Tag(name = "Authentication", description = "User authentication and registration")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    /**
     * Authenticate user and generate JWT token.
     */
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT token"
    )
    @ApiResponse(responseCode = "200", description = "Login successful, token returned")
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        String token = jwtUtil.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * Register new user account.
     */
    @Operation(
            summary = "User registration",
            description = "Creates a new user account with USER role"
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse registeredUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
}