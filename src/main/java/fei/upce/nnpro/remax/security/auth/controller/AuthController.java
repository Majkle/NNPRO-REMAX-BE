package fei.upce.nnpro.remax.security.auth.controller;

import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.security.auth.request.AuthRequest;
import fei.upce.nnpro.remax.security.auth.request.PasswordResetConfirmRequest;
import fei.upce.nnpro.remax.security.auth.request.PasswordResetRequest;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import fei.upce.nnpro.remax.security.auth.response.AuthResponse;
import fei.upce.nnpro.remax.security.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for login, registration, and password management")
public class AuthController {

    private final AuthService authService;
    private final RemaxUserRepository userRepository;

    public AuthController(AuthService authService, RemaxUserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials or account blocked", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register new user", description = "Registers a new Client account. Realtors/Admins must be created by Admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemaxUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or Username/Email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RemaxUser user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get current user", description = "Retrieves the profile of the currently authenticated user based on the JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemaxUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content),
            @ApiResponse(responseCode = "404", description = "User data not found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Request password reset", description = "Generates a reset code and sends it via email (if configured).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset request processed (returns success GIF URL)"),
            @ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content)
    })
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().body("https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExdnM0dDl2czd1YXdjY3NpMDNtNTlmcTJvMXV0NDBuaTVib2c1eDhnMyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/XZgeQHl4zAnmmh1vi3/giphy.gif");
    }

    @Operation(summary = "Confirm password reset", description = "Resets the password using the code received via email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully changed"),
            @ApiResponse(responseCode = "400", description = "Invalid code, expired code, or invalid username",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.getUsername(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
