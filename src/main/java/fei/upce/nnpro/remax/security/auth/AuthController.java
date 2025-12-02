package fei.upce.nnpro.remax.security.auth;

import fei.upce.nnpro.remax.model.users.RemaxUser;
import fei.upce.nnpro.remax.model.users.RemaxUserRepository;
import fei.upce.nnpro.remax.security.auth.request.AuthRequest;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import fei.upce.nnpro.remax.security.auth.response.AuthResponse;
import fei.upce.nnpro.remax.security.auth.request.PasswordResetRequest;
import fei.upce.nnpro.remax.security.auth.request.PasswordResetConfirmRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RemaxUserRepository userRepository;

    public AuthController(AuthService authService, RemaxUserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RemaxUser user = authService.register(request);
        return ResponseEntity.ok(user);
    }

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

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.getCode(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
