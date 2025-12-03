package fei.upce.nnprop.remax.security.auth;

import fei.upce.nnprop.remax.model.users.RemaxUser;
import fei.upce.nnprop.remax.model.users.RemaxUserRepository;
import fei.upce.nnprop.remax.security.auth.request.AuthRequest;
import fei.upce.nnprop.remax.security.auth.request.RegisterRequest;
import fei.upce.nnprop.remax.security.auth.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and Registration")
public class AuthController {

    private final AuthService authService;
    private final RemaxUserRepository userRepository;

    public AuthController(AuthService authService, RemaxUserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Login", description = "Authenticates user and returns JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or blocked account")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register Client", description = "Registers a new user with Client role")
    @ApiResponse(responseCode = "200", description = "Registration successful")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RemaxUser user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get Current User", description = "Returns details of the currently logged-in user")
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
}
